/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib.commands;

/**
 *
 * @author hamish
 */
public interface Command {

    void runCommand() throws Exception;

    void runCommand(String[] args) throws Exception;
}
