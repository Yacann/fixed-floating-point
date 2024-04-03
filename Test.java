import java.util.Random;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        Random rand = new Random(0);
        int i1, i2;
        SinglePrecision s1, s2;
        float f1, f2;
        for (int i = 0; i < 10000; i++) {
            i1 = rand.nextInt(); i2 = rand.nextInt();
            s1 = new SinglePrecision(i1); s2 = new SinglePrecision(i2);
            f1 = Float.intBitsToFloat(i1); f2 = Float.intBitsToFloat(i2);
            if (!s1.add(s2).toHexString().equals(Float.toHexString(f1 + f2))) {
                System.out.println("0".repeat(Integer.numberOfLeadingZeros(i1)) + Integer.toBinaryString(i1) + " + ");
                System.out.println("0".repeat(Integer.numberOfLeadingZeros(i2)) + Integer.toBinaryString(i2));
                System.out.println("Sum of s1 " + s1.toHexString() +
                                    " and s2 "+ s2.toHexString() +
                                    " equals: " + s1.add(s2).toHexString());
                System.out.println("Sum of f1 " + Float.toHexString(f1) +
                                    " and f2 "+ Float.toHexString(f2) +
                                    " equals: " + Float.toHexString(f1 + f2));
                System.out.println();
            }
        }
    }
}
