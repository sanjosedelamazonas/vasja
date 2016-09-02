package org.json.simple;

import java.io.IOException;
import java.io.Writer;

public abstract interface JSONStreamAware
{
  public abstract void writeJSONString(Writer paramWriter)
    throws IOException;
}

/* Location:           /opt/workspace_vasja/DotMatrixPrinter/classes/
 * Qualified Name:     org.json.simple.JSONStreamAware
 * JD-Core Version:    0.6.0
 */