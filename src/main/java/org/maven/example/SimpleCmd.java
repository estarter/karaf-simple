package org.maven.example;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

/**
 * @author Alexey Merezhin
 */
@Command(scope = "simple", name = "action", description = "Test command for Simple class")
public class SimpleCmd extends OsgiCommandSupport {
    private Simple simple;

    @Argument(index = 0, name = "arg", description = "The command argument", required = false, multiValued = false)
    String arg = null;

    public SimpleCmd(Simple simple) {
        this.simple = simple;
    }

    @Override
    protected Object doExecute() throws Exception {
        simple.doAction();
        return null;
    }
}
