import java.util.InputMismatchException;
import java.util.stream.Collectors;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if ((args.length != 3 && args.length != 5) || !args[1].equals("1")
            || !args[2].startsWith("0x") ||
            (args.length == 5 && (!args[4].startsWith("0x") || !"+-*/".contains(args[3]))))
        {
            throw new InputMismatchException("unexpected input: " + Arrays.stream(args).collect(Collectors.toList()));
        }
        if (args[0].equals("f")) {
            SinglePrecision s1 = new SinglePrecision(Integer.parseUnsignedInt(args[2].substring(2), 16));
            if (args.length == 3) System.out.println(s1);
            else {
                SinglePrecision s2 = new SinglePrecision(Integer.parseUnsignedInt(args[4].substring(2), 16));
                if (args[3].equals("+")) System.out.println(s1.add(s2));
                else if (args[3].equals("-")) System.out.println(s1.subtract(s2));
                else if (args[3].equals("*")) System.out.println(s1.multiply(s2));
                else System.out.println(s1.divide(s2));
            }
        } else if (args[0].equals("h")) {
            HalfPrecision h1 = new HalfPrecision(Integer.parseUnsignedInt(args[2].substring(2), 16));
            if (args.length == 3) System.out.println(h1);
            else {
                HalfPrecision h2 = new HalfPrecision(Integer.parseUnsignedInt(args[4].substring(2), 16));
                if (args[3].equals("+")) System.out.println(h1.add(h2));
                else if (args[3].equals("-")) System.out.println(h1.subtract(h2));
                else if (args[3].equals("*")) System.out.println(h1.multiply(h2));
                else System.out.println(h1.divide(h2));
            }
        } else {
            try {
                int A = Integer.parseInt(args[0].substring(0, args[0].indexOf("."))),
                B = Integer.parseInt(args[0].substring(args[0].indexOf(".")+1));
                FixedPoint f1 = new FixedPoint(A, B, Integer.parseUnsignedInt(args[2].substring(2), 16));
                if (args.length == 3) System.out.println(f1);
                else {
                    FixedPoint f2 = new FixedPoint(A, B, Integer.parseUnsignedInt(args[4].substring(2), 16));
                    if (args[3].equals("+")) System.out.println(f1.add(f2));
                    else if (args[3].equals("-")) System.out.println(f1.subtract(f2));
                    else if (args[3].equals("*")) System.out.println(f1.multiply(f2));
                    else System.out.println(f1.divide(f2));
                }
            } catch (NumberFormatException e) {
                System.err.println(e.getMessage());
            } catch (ArithmeticException e) {
                System.err.println("error");
            }
        }
    }
}