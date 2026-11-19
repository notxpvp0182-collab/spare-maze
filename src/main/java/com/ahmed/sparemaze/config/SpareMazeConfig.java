package com.ahmed.sparemaze.config;

public class SpareMazeConfig {
    public boolean masterEnable = true;
    public boolean notifications = false;
    public boolean debugMode = false;
    public boolean predictionDiagnostics = false;
    public boolean stateInformation = false;
    public boolean safeMode = true;

    public PearlConfig pearl = new PearlConfig();
    public MaceConfig mace = new MaceConfig();
    public AxeConfig axe = new AxeConfig();
    public SpearConfig spear = new SpearConfig();
}
