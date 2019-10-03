package cognos.tm1.ti;

public enum ProcessReturnCode {
	PROCESS_EXIT_ON_INIT (12),
	PROCESS_EXIT_NORMAL (0),
	PROCESS_EXIT_BY_BREAK (8),
	PROCESS_EXIT_SERIOUS_ERROR (11),
	PROCESS_EXIT_MINOR_ERROR (7),
	PROCESS_EXIT_BY_QUIT (9),
	PROCESS_EXIT_BY_CHORE_QUIT (17),
	PROCESS_EXIT_WITH_MESSAGE (5),
	PROCESS_EXIT_ROLLBACK (2),
	PROCESS_EXIT_UNKNOWN (1),
	PROCESS_IN_PROGRESS (-1),
	EXECUTION_TIMEOUT (-2);

    private final int code;

    private ProcessReturnCode(int code) {
    	this.code = code;
    }

    public int toInt() {
    	return code;
    }
}
