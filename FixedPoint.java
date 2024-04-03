import java.math.BigInteger;

public class FixedPoint {
    protected final int A;
    protected final int B;
    private final int S;
    protected final int num;
    private final int decOutputPrecision = 3;

    public FixedPoint(int A, int B, int num) {
        this.A = A;
        this.B = B;
        this.S = 32 - A - B;
        this.num = num;
    }

    public FixedPoint add(FixedPoint other) {
        return new FixedPoint(A, B, (num << S) + (other.num << S) >> S & (A + B == 32 ? -1 : (1 << A + B) - 1));
    }

    public FixedPoint subtract(FixedPoint other) {
        return new FixedPoint(A, B, (num << S) - (other.num << S) >> S & (A + B == 32 ? -1 : (1 << A + B) - 1));
    }

    public FixedPoint multiply(FixedPoint other) {
        return new FixedPoint(A, B, (int) round(((long) (num << S)) * ((long) (other.num << S)) >> 2 * S, B) & (A + B == 32 ? -1 : (1 << A + B) - 1));
    }

    public FixedPoint divide(FixedPoint other) {
        if (other.num == 0) throw new ArithmeticException("division by zero");
        return new FixedPoint(A, B, (int) round(((long) (num << S) << 32) / (long) (other.num << S), 32 - B) & (A + B == 32 ? -1 : (1 << A + B) - 1));
    }

    private long round(long num, int shift) {
        if (((num >> shift & 1) == 1 || Long.numberOfTrailingZeros(num) < shift-1)
            && (num >> shift-1 & 1) == 1) num += 1 << shift;
        return num >> shift;
    }

    public String toString() {
        String sign; int out;
        if (num >> A + B - 1 == 1) {
            sign = "-"; out = (1 << A + B - 1) - (num & (1 << A + B - 1)-1);
        } else {
            sign = ""; out = num;
        }
        BigInteger digits = BigInteger.valueOf(out & (1 << B) - 1).multiply(BigInteger.valueOf(5).pow(Integer.max(B, decOutputPrecision)));
        BigInteger pow = BigInteger.TEN.pow(Integer.max(B - decOutputPrecision, 0));
        BigInteger[] qnr = digits.divideAndRemainder(pow);
        int compare = qnr[1].multiply(BigInteger.TWO).compareTo(pow);
        if (compare >= 0) {
            if (compare == 1) qnr[0] = qnr[0].add(BigInteger.ONE);
            else if (qnr[0].testBit(0)) qnr[0] = qnr[0].add(BigInteger.ONE);
        }
        StringBuilder result = new StringBuilder();
        return result.append(sign).append((out >> B) + (qnr[0].compareTo(BigInteger.TEN.pow(decOutputPrecision)) == 0 ? 1 : 0))
            .append(".").append(qnr[0].compareTo(BigInteger.TEN.pow(decOutputPrecision)) == 0 ? 0 : qnr[0].intValue()).toString();
    }
}
