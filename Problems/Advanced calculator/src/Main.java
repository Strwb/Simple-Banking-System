import java.util.Arrays;

/* Please, do not rename it */
class Problem {

    public static void main(String[] args) {
        String operator = args[0];
        int[] numbers = Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).mapToInt(Integer::parseInt).toArray();
        int result = 0;
        switch (operator) {
            case "MAX":
                result = max(numbers);
                break;
            case "MIN":
                result = min(numbers);
                break;
            case "SUM":
                result = sum(numbers);
                break;
            default:
                break;
        }
        System.out.println(result);
    }

    private static int max(int[] numbers) {
        int max = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (max < numbers[i]) {
                max = numbers[i];
            }
        }
        return max;
    }

    private static int min(int[] numbers) {
        int min = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (min > numbers[i]) {
                min = numbers[i];
            }
        }
        return min;
    }

    private static int sum(int[] numbers) {
        int sum = 0;
        for (int num : numbers) {
            sum += num;
        }
        return sum;
    }
}
