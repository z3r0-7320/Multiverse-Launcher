package multiverse;

public class ProgramState {

    public enum ProgramStateEnum {
        CREATE_PROFILE,
        EDIT_PROFILE,
        OTHER
    }

    private static ProgramStateEnum currentStatus;

    static  {
        ProgramState.currentStatus = ProgramStateEnum.OTHER;
    }

    public static ProgramStateEnum getCurrentStatus() {
        return ProgramState.currentStatus;
    }

    public static void updateStatus(ProgramStateEnum newStatus) {
        ProgramState.currentStatus = newStatus;
    }
}
