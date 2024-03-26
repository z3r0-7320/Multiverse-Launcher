package multiverse;

public class ProgramState {

    private static ProgramStateEnum currentStatus;

    static {
        ProgramState.currentStatus = ProgramStateEnum.OTHER;
    }

    public static ProgramStateEnum getCurrentStatus() {
        return ProgramState.currentStatus;
    }

    public static void updateStatus(ProgramStateEnum newStatus) {
        ProgramState.currentStatus = newStatus;
    }

    public enum ProgramStateEnum {
        CREATE_PROFILE,
        EDIT_PROFILE,
        OTHER
    }
}
