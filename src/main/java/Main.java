import java.awt.image.AreaAveragingScaleFilter;
import java.util.*;
import java.util.concurrent.*;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//Logger logger = LoggerFactory.getLogger(Main.class);
//logger.info("Hello World");

public class Main {

    public static int[] sort(int[] arr, int begin, int end) {
        if (begin < end) {
            int q = (begin + end) / 2;
            sort(arr, begin, q);
            sort(arr, q + 1, end);
            merge(arr, begin, q, end);
        }
        return arr;
    }

    public static void merge(int[] arr, int begin, int q, int end) {
        if (arr.length > 2) {
            for (int i = begin; i < end; ++ i) {
                int minValue = arr[i];
                int minIndex = i;

                for (int j = i + 1; j < end; ++j) {
                    if (minValue > arr[j]) {
                        minValue = arr[j];
                        minIndex = j;
                    }
                }

                if (minIndex != i) {
                    int temp = arr[minIndex];
                    arr[minIndex] = arr[i];
                    arr[i] = temp;
                }
            }
        } else if (arr.length > 1 && arr[0] > arr[1]) {
            int temp = arr[0];
            arr[0] = arr[1];
            arr[1] = arr[0];
        }
    }

    public static void main(String[] args) {
        int[] arr = {0, 10, 3, 8, 2, 6, 1, 32, 33, 0};
        int[] newArr = sort(arr, 0, arr.length);
        for (int el: arr)
            System.out.print(el + ", ");
    }
}
