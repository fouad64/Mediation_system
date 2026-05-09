/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mideation;

/**
 *
 * @author roqaya
 */

public class MediationRule {

    private int sourceNodeId;
    private int destinationNodeId;

    public MediationRule(int sourceNodeId,
                         int destinationNodeId) {

        this.sourceNodeId = sourceNodeId;
        this.destinationNodeId = destinationNodeId;
    }

    public int getSourceNodeId() {
        return sourceNodeId;
    }

    public int getDestinationNodeId() {
        return destinationNodeId;
    }
}
