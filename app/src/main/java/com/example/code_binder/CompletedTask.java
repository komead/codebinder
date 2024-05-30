package com.example.code_binder;

import java.util.ArrayList;

public class CompletedTask {
    private String proposal_id;
    ArrayList<String> scannedCodes;

    public CompletedTask(String proposal_id, ArrayList<String> scannedCodes) {
        this.proposal_id = proposal_id;
        this.scannedCodes = scannedCodes;
    }

    public String getProposal_id() {
        return proposal_id;
    }

    public ArrayList<String> getScannedCodes() {
        return scannedCodes;
    }
}
