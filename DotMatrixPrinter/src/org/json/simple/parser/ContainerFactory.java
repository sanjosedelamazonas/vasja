package org.json.simple.parser;

import java.util.List;
import java.util.Map;

public abstract interface ContainerFactory
{
  public abstract Map createObjectContainer();

  public abstract List creatArrayContainer();
}

/* Location:           /opt/workspace_vasja/DotMatrixPrinter/classes/
 * Qualified Name:     org.json.simple.parser.ContainerFactory
 * JD-Core Version:    0.6.0
 */