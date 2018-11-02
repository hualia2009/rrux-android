package com.ucredit.dream.listener;

public class ApplyStateEvent {
    
    private int applyState;
    
    public ApplyStateEvent(int applyState){
        this.applyState = applyState;
    }

    public int getApplyState() {
        return applyState;
    }

    public void setApplyState(int applyState) {
        this.applyState = applyState;
    }

    

}
