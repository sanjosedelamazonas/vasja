package org.json.simple;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONValue
{
  public static Object parse(Reader paramReader)
  {
    try
    {
      JSONParser localJSONParser = new JSONParser();
      return localJSONParser.parse(paramReader);
    }
    catch (Exception localException)
    {
    }
    return null;
  }

  public static Object parse(String paramString)
  {
    StringReader localStringReader = new StringReader(paramString);
    return parse(localStringReader);
  }

  public static Object parseWithException(Reader paramReader)
    throws IOException, ParseException
  {
    JSONParser localJSONParser = new JSONParser();
    return localJSONParser.parse(paramReader);
  }

  public static Object parseWithException(String paramString)
    throws ParseException
  {
    JSONParser localJSONParser = new JSONParser();
    return localJSONParser.parse(paramString);
  }

  public static void writeJSONString(Object paramObject, Writer paramWriter)
    throws IOException
  {
    if (paramObject == null)
    {
      paramWriter.write("null");
      return;
    }
    if ((paramObject instanceof String))
    {
      paramWriter.write(34);
      paramWriter.write(escape((String)paramObject));
      paramWriter.write(34);
      return;
    }
    if ((paramObject instanceof Double))
    {
      if ((((Double)paramObject).isInfinite()) || (((Double)paramObject).isNaN()))
        paramWriter.write("null");
      else
        paramWriter.write(paramObject.toString());
      return;
    }
    if ((paramObject instanceof Float))
    {
      if ((((Float)paramObject).isInfinite()) || (((Float)paramObject).isNaN()))
        paramWriter.write("null");
      else
        paramWriter.write(paramObject.toString());
      return;
    }
    if ((paramObject instanceof Number))
    {
      paramWriter.write(paramObject.toString());
      return;
    }
    if ((paramObject instanceof Boolean))
    {
      paramWriter.write(paramObject.toString());
      return;
    }
    if ((paramObject instanceof JSONStreamAware))
    {
      ((JSONStreamAware)paramObject).writeJSONString(paramWriter);
      return;
    }
    if ((paramObject instanceof JSONAware))
    {
      paramWriter.write(((JSONAware)paramObject).toJSONString());
      return;
    }
    if ((paramObject instanceof Map))
    {
      JSONObject.writeJSONString((Map)paramObject, paramWriter);
      return;
    }
    if ((paramObject instanceof List))
    {
      JSONArray.writeJSONString((List)paramObject, paramWriter);
      return;
    }
    paramWriter.write(paramObject.toString());
  }

  public static String toJSONString(Object paramObject)
  {
    if (paramObject == null)
      return "null";
    if ((paramObject instanceof String))
      return "\"" + escape((String)paramObject) + "\"";
    if ((paramObject instanceof Double))
    {
      if ((((Double)paramObject).isInfinite()) || (((Double)paramObject).isNaN()))
        return "null";
      return paramObject.toString();
    }
    if ((paramObject instanceof Float))
    {
      if ((((Float)paramObject).isInfinite()) || (((Float)paramObject).isNaN()))
        return "null";
      return paramObject.toString();
    }
    if ((paramObject instanceof Number))
      return paramObject.toString();
    if ((paramObject instanceof Boolean))
      return paramObject.toString();
    if ((paramObject instanceof JSONAware))
      return ((JSONAware)paramObject).toJSONString();
    if ((paramObject instanceof Map))
      return JSONObject.toJSONString((Map)paramObject);
    if ((paramObject instanceof List))
      return JSONArray.toJSONString((List)paramObject);
    return paramObject.toString();
  }

  public static String escape(String paramString)
  {
    if (paramString == null)
      return null;
    StringBuffer localStringBuffer = new StringBuffer();
    escape(paramString, localStringBuffer);
    return localStringBuffer.toString();
  }

  static void escape(String paramString, StringBuffer paramStringBuffer)
  {
    for (int i = 0; i < paramString.length(); i++)
    {
      int j = paramString.charAt(i);
      switch (j)
      {
      case 34:
        paramStringBuffer.append("\\\"");
        break;
      case 92:
        paramStringBuffer.append("\\\\");
        break;
      case 8:
        paramStringBuffer.append("\\b");
        break;
      case 12:
        paramStringBuffer.append("\\f");
        break;
      case 10:
        paramStringBuffer.append("\\n");
        break;
      case 13:
        paramStringBuffer.append("\\r");
        break;
      case 9:
        paramStringBuffer.append("\\t");
        break;
      case 47:
        paramStringBuffer.append("\\/");
        break;
      default:
        if (((j >= 0) && (j <= 31)) || ((j >= 127) && (j <= 159)) || ((j >= 8192) && (j <= 8447)))
        {
          String str = Integer.toHexString(j);
          paramStringBuffer.append("\\u");
          for (int k = 0; k < 4 - str.length(); k++)
            paramStringBuffer.append('0');
          paramStringBuffer.append(str.toUpperCase());
        }
        else
        {
          paramStringBuffer.append(j);
        }
      }
    }
  }
}

/* Location:           /opt/workspace_vasja/DotMatrixPrinter/classes/
 * Qualified Name:     org.json.simple.JSONValue
 * JD-Core Version:    0.6.0
 */