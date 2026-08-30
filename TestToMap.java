import java.util.stream.*;
import java.util.*;

public class TestToMap {
    public static void main(String[] args) {
        try {
            List<String> list = Arrays.asList("A", "B");
            Map<String, String> map = list.stream().collect(Collectors.toMap(s -> s.equals("A") ? null : s, s -> s));
            System.out.println("Success: " + map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
