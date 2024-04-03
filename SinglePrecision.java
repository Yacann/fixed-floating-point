import java.math.BigInteger;

public class SinglePrecision {
    public final int mantissa;
    public final int exp;
    public final int sign;
    private final int expSize = 8;
    private final int mantissaSize = 23;
    private final int hexOutputPrecision = 6; 

    public SinglePrecision(int bits) {
        this.mantissa = bits & ((1 << mantissaSize) - 1);
        bits >>= mantissaSize;
        this.exp = bits & ((1 << expSize) - 1);
        bits >>= expSize;
        this.sign = bits & 1;
    }

    private SinglePrecision(int sign, int exp, int mantissa) {
        this.sign = sign;
        this.exp = exp;
        this.mantissa = mantissa;
    }

    public SinglePrecision add(SinglePrecision other) {
        if (isNaN() || other.isNaN() || isInf() && other.isInf() && sign != other.sign) return getNaN();
        if (isInf()) return getInf(sign);
        if (other.isInf()) return getInf(other.sign);
        int e1 = exp + (isSubnormal() ? 1 : 0), e2 = other.exp + (other.isSubnormal() ? 1 : 0);
        BigInteger bits1 = BigInteger.valueOf(mantissa + (isSubnormal() || isZero() ? 0 : 1 << mantissaSize));
        if (e1 > e2) bits1 = bits1.shiftLeft(e1 - e2);
        if (sign == 1) bits1 = bits1.negate();
        BigInteger bits2 = BigInteger.valueOf(other.mantissa + (other.isSubnormal() || other.isZero() ? 0 : 1 << mantissaSize));
        if (e2 > e1) bits2 = bits2.shiftLeft(e2 - e1);
        if (other.sign == 1) bits2 = bits2.negate();
        BigInteger bits = bits1.add(bits2); int newExp = Integer.min(e1, e2);
        int newSign = bits.signum() == -1 ? 1 : 0; bits = bits.abs();
        if (bits == BigInteger.ZERO) return getZero(sign == 1 && other.sign == 1 ? 1 : 0);
        int shift = Integer.max(bits.bitLength() - mantissaSize - 1, 1 - newExp);
        newExp += shift; bits = round(bits, shift);
        if (bits.testBit(mantissaSize + 1)) { newExp++; bits = bits.shiftRight(1);}
        if (newExp + 1 == 1 << expSize) return getInf(newSign);
        else if (newExp <= 0) return getZero(newSign);
        if (bits.testBit(mantissaSize)) bits = bits.flipBit(mantissaSize); else newExp--;
        return new SinglePrecision(newSign, newExp, bits.intValue());
    }

    public SinglePrecision subtract(SinglePrecision other) {
        if (isNaN() || other.isNaN() || isInf() && other.isInf() && sign == other.sign) return getNaN();
        if (isInf()) return getInf(sign);
        if (other.isInf()) return getInf(other.sign);
        int e1 = exp + (isSubnormal() ? 1 : 0), e2 = other.exp + (other.isSubnormal() ? 1 : 0);
        BigInteger bits1 = BigInteger.valueOf(mantissa + (isSubnormal() || isZero() ? 0 : 1 << mantissaSize));
        if (e1 > e2) bits1 = bits1.shiftLeft(e1 - e2);
        if (sign == 1) bits1 = bits1.negate();
        BigInteger bits2 = BigInteger.valueOf(other.mantissa + (other.isSubnormal() || other.isZero() ? 0 : 1 << mantissaSize));
        if (e2 > e1) bits2 = bits2.shiftLeft(e2 - e1);
        if (other.sign == 1) bits2 = bits2.negate();
        BigInteger bits = bits1.subtract(bits2); int newExp = Integer.min(e1, e2);
        int newSign = bits.signum() == -1 ? 1 : 0; bits = bits.abs();
        if (bits == BigInteger.ZERO) return getZero(sign == 1 && other.sign == 0 ? 1 : 0);
        int shift = Integer.max(bits.bitLength() - mantissaSize - 1, 1 - newExp);
        newExp += shift; bits = round(bits, shift);
        if (bits.testBit(mantissaSize + 1)) { newExp++; bits = bits.shiftRight(1);}
        if (newExp + 1 == 1 << expSize) return getInf(newSign);
        else if (newExp <= 0) return getZero(newSign);
        if (bits.testBit(mantissaSize)) bits = bits.flipBit(mantissaSize); else newExp--;
        return new SinglePrecision(newSign, newExp, bits.intValue());
    }

    public SinglePrecision multiply(SinglePrecision other) {
        if (isNaN() || other.isNaN() || isInf() && other.isZero() || isZero() && other.isInf()) return getNaN();
        if (isInf() || other.isInf()) return getInf(sign ^ other.sign);
        if (isZero() || other.isZero()) return getZero(sign ^ other.sign);
        BigInteger bits1 = BigInteger.valueOf(mantissa + (isSubnormal() || isZero() ? 0 : 1 << mantissaSize));
        if (sign == 1) bits1 = bits1.negate();
        BigInteger bits2 = BigInteger.valueOf(other.mantissa + (other.isSubnormal() || other.isZero() ? 0 : 1 << mantissaSize));
        if (other.sign == 1) bits2 = bits2.negate();
        BigInteger bits = bits1.multiply(bits2);
        int newSign = bits.signum() == -1 ? 1 : 0; bits = bits.abs();
        int newExp = exp + (isSubnormal() ? 1 : 0) + other.exp + (other.isSubnormal() ? 1 : 0) + 1 - (1 << expSize-1);
        int shift = Integer.max(bits.bitLength() - 2 * mantissaSize - 1, 1 - newExp);
        newExp += shift; bits = round(bits, shift + mantissaSize);
        if (bits.testBit(mantissaSize + 1)) { newExp++; bits = bits.shiftRight(1);}
        if (newExp + 1 >= 1 << expSize) return getInf(newSign);
        else if (newExp <= 0) return getZero(newSign);
        if (bits.testBit(mantissaSize)) bits = bits.flipBit(mantissaSize); else newExp--;
        return new SinglePrecision(newSign, newExp, bits.intValue());

    }

    public SinglePrecision divide(SinglePrecision other) {
        if (isNaN() || other.isNaN() || isInf() && other.isInf() || isZero() && other.isZero()) return getNaN();
        if (isInf() || other.isZero()) return getInf(sign ^ other.sign);
        if (isZero() || other.isInf()) return getZero(sign ^ other.sign);
        BigInteger bits1 = BigInteger.valueOf(mantissa + (isSubnormal() || isZero() ? 0 : 1 << mantissaSize));
        bits1 = bits1.shiftLeft(3 * mantissaSize); if (sign == 1) bits1 = bits1.negate();
        BigInteger bits2 = BigInteger.valueOf(other.mantissa + (other.isSubnormal() || other.isZero() ? 0 : 1 << mantissaSize));
        if (other.sign == 1) bits2 = bits2.negate();
        BigInteger bits = bits1.divide(bits2);
        int newSign = bits.signum() == -1 ? 1 : 0; bits = bits.abs();
        int newExp = exp + (isSubnormal() ? 1 : 0) - other.exp - (other.isSubnormal() ? 1 : 0) - 1 + (1 << expSize-1);
        int shift = Integer.max(bits.bitLength() - 3 * mantissaSize - 1, 1 - newExp);
        newExp += shift; bits = round(bits, shift + 2 * mantissaSize);
        if (bits.testBit(mantissaSize + 1)) { newExp++; bits = bits.shiftRight(1);}
        if (newExp + 1 >= 1 << expSize) return getInf(newSign);
        else if (newExp <= 0) return getZero(newSign);
        if (bits.testBit(mantissaSize)) bits = bits.flipBit(mantissaSize); else newExp--;
        return new SinglePrecision(newSign, newExp, bits.intValue());
    }

    private BigInteger round(BigInteger num, int shift) {
        if (shift > 0 && num.testBit(shift - 1) && (num.testBit(shift) || num.getLowestSetBit() < shift - 1)) {
            num = num.add(BigInteger.ONE.shiftLeft(shift));
        }
        return num.shiftRight(shift);
    }

    public String toHexString() {
        if (isNaN()) return "NaN";
        if (isInf()) return sign == 0 ? "Infinity" : "-Infinity";
        if (isZero()) return sign == 0 ? "0x0.0p0" : "-0x0.0p0";
        String hex = BigInteger.valueOf(mantissa).shiftRight(mantissaSize - 4 * hexOutputPrecision).toString(16);
        hex = "0".repeat(hexOutputPrecision - hex.length()) + hex;
        int ind = hexOutputPrecision; for (; ind > 1 && hex.charAt(ind - 1) == '0'; ind--);
        StringBuilder result = new StringBuilder();
        result.append(sign == 0 ? "0x" : "-0x")
                .append(isSubnormal() ? "0." : "1.")
                .append(hex.substring(0, ind))
                .append("p")
                .append((isSubnormal() ? exp + 2 : exp + 1) - (1 << expSize - 1));
        return result.toString();
    }

    @Override
    public String toString() {
        if (isNaN()) return "nan";
        if (isInf()) return sign == 0 ? "inf" : "-inf";
        if (isZero()) return sign == 0 ? "0x0.000000p+0" : "-0x0.000000p+0";
        int out = mantissa << 1, shift = 0;
        if (isSubnormal()) { shift = Integer.numberOfLeadingZeros(out) - 7; out <<= shift; out -= 1 << 24; }
        String hex = Integer.toHexString(out);
        hex = "0".repeat(6 - hex.length()) + hex;
        StringBuilder result = new StringBuilder();
        result  .append(sign == 0 ? "0x" : "-0x")
                .append("1.")
                .append(hex)
                .append(exp + 1 >= (1 << expSize-1) ? "p+" : "p")
                .append((isSubnormal() ? exp + 2 : exp + 1) - (1 << expSize-1) - shift);
        return result.toString();
    }

    public boolean isInf() {
        return exp + 1 >> expSize == 1 && mantissa == 0;
    }

    public SinglePrecision getInf(int sign) {
        return new SinglePrecision(sign, (1 << expSize) - 1, 0);
    }

    public boolean isNaN() {
        return exp + 1 >> expSize == 1 && mantissa != 0;
    }

    public SinglePrecision getNaN() {
        return new SinglePrecision(0, (1 << expSize) - 1, (1 << mantissaSize - 1));
    }

    public boolean isZero() {
        return exp == 0 && mantissa == 0;
    }

    public SinglePrecision getZero(int sign) {
        return new SinglePrecision(sign, 0, 0);
    }

    public boolean isSubnormal() {
        return exp == 0 && mantissa != 0;
    }
}
