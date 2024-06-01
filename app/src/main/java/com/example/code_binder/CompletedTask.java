package com.example.code_binder;

import java.util.ArrayList;

public class CompletedTask {
    private String proposal_id;
    private ArrayList<String> gtins;

    public CompletedTask(String proposal_id, ArrayList<String> gtins) {
        this.proposal_id = proposal_id;
        this.gtins = new ArrayList<>();

        for (String string : gtins)
            this.gtins.add(string.substring(4, 17));
    }

    public void setProposal_id(String proposal_id) {
        this.proposal_id = proposal_id;
    }

    public void setGtins(ArrayList<String> gtins) {
        this.gtins = gtins;
    }

    public String getProposal_id() {
        return proposal_id;
    }

    public ArrayList<String> getGtins() {
        return gtins;
    }
}
