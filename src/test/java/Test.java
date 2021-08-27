import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        StringBuilder builder = new StringBuilder();
        while (scan.hasNext()) {
            builder.append(scan.next() + " ");
        }
        System.out.println(builder);
    }
}
