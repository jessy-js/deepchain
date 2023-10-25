import com.paillier.utils.WashVector;

import java.math.BigDecimal;
import java.util.List;

public class TestWV {
    public static void main(String[] args) {
        WashVector wv = new WashVector("C:\\Users\\lenovo\\Desktop\\deepchain-kotlin\\data\\worker 6\\worker 2.txt");
        List<List<BigDecimal>> vectorList = wv.wash(20);
        wv.unwash(vectorList, "C:\\Users\\lenovo\\Desktop\\deepchain-kotlin\\data\\output\\worker 2.txt");
    }
}
