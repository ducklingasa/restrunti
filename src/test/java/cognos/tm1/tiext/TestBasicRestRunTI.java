package cognos.tm1.tiext;

import static cognos.tm1.ti.ProcessReturnCode.PROCESS_EXIT_NORMAL;
import static cognos.tm1.ti.ProcessReturnCode.PROCESS_IN_PROGRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

public class TestBasicRestRunTI {

	public static void main(String[] args) {
		System.out.println("Result: " + BasicRestRunTI.BasicRestRunTI(args));
	}

	@Test
	@Order(1)
    public void test_A_SyncRunTI() {
        assertEquals(PROCESS_EXIT_NORMAL.toInt(), BasicRestRunTI.BasicRestRunTI(new String[]{"https://localhost:8030","1","}Basic","YWRtaW46YXBwbGU=","z_fix"}));
    }

	@Test
	@Order(2)
    public void test_B_AsyncRunTI() {
        assertEquals(PROCESS_IN_PROGRESS.toInt(), BasicRestRunTI.BasicRestRunTI(new String[]{"https://localhost:8030","0","ADMIN","apple","z_fix"}));
    }

	@AfterAll
    public static void finish_Testing() throws InterruptedException {
		Thread.sleep(10000);
    }
}
