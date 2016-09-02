package org.json.simple.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

class Yylex
{
  public static final int YYEOF = -1;
  private static final int ZZ_BUFFERSIZE = 16384;
  public static final int YYINITIAL = 0;
  public static final int STRING_BEGIN = 2;
  private static final int[] ZZ_LEXSTATE = { 0, 0, 1, 1 };
  private static final String ZZ_CMAP_PACKED = "";
  private static final char[] ZZ_CMAP = zzUnpackCMap("");
  private static final int[] ZZ_ACTION = zzUnpackAction();
  private static final String ZZ_ACTION_PACKED_0 = "";
  private static final int[] ZZ_ROWMAP = zzUnpackRowMap();
  private static final String ZZ_ROWMAP_PACKED_0 = "";
  private static final int[] ZZ_TRANS = { 2, 2, 3, 4, 2, 2, 2, 5, 2, 6, 2, 2, 7, 8, 2, 9, 2, 2, 2, 2, 2, 10, 11, 12, 13, 14, 15, 16, 16, 16, 16, 16, 16, 16, 16, 17, 18, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, 19, 20, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 20, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 21, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 22, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 23, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 16, 16, 16, 16, 16, 16, 16, 16, -1, -1, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, -1, -1, -1, -1, -1, -1, -1, -1, 24, 25, 26, 27, 28, 29, 30, 31, 32, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 33, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 34, 35, -1, -1, 34, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 36, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 37, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 38, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 39, -1, 39, -1, 39, -1, -1, -1, -1, -1, 39, 39, -1, -1, -1, -1, 39, 39, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 33, -1, 20, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 20, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 35, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 38, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 40, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 41, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 42, -1, 42, -1, 42, -1, -1, -1, -1, -1, 42, 42, -1, -1, -1, -1, 42, 42, -1, -1, -1, -1, -1, -1, -1, -1, -1, 43, -1, 43, -1, 43, -1, -1, -1, -1, -1, 43, 43, -1, -1, -1, -1, 43, 43, -1, -1, -1, -1, -1, -1, -1, -1, -1, 44, -1, 44, -1, 44, -1, -1, -1, -1, -1, 44, 44, -1, -1, -1, -1, 44, 44, -1, -1, -1, -1, -1, -1, -1, -1 };
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;
  private static final String[] ZZ_ERROR_MSG = { "Unkown internal scanner error", "Error: could not match input", "Error: pushback value was too large" };
  private static final int[] ZZ_ATTRIBUTE = zzUnpackAttribute();
  private static final String ZZ_ATTRIBUTE_PACKED_0 = "";
  private Reader zzReader;
  private int zzState;
  private int zzLexicalState = 0;
  private char[] zzBuffer = new char[16384];
  private int zzMarkedPos;
  private int zzCurrentPos;
  private int zzStartRead;
  private int zzEndRead;
  private int yyline;
  private int yychar;
  private int yycolumn;
  private boolean zzAtBOL = true;
  private boolean zzAtEOF;
  private StringBuffer sb = new StringBuffer();

  private static int[] zzUnpackAction()
  {
    int[] arrayOfInt = new int[45];
    int i = 0;
    i = zzUnpackAction("", i, arrayOfInt);
    return arrayOfInt;
  }

  private static int zzUnpackAction(String paramString, int paramInt, int[] paramArrayOfInt)
  {
    int i = 0;
    int j = paramInt;
    int k = paramString.length();
    while (i < k)
    {
      int m = paramString.charAt(i++);
      int n = paramString.charAt(i++);
      do
      {
        paramArrayOfInt[(j++)] = n;
        m--;
      }
      while (m > 0);
    }
    return j;
  }

  private static int[] zzUnpackRowMap()
  {
    int[] arrayOfInt = new int[45];
    int i = 0;
    i = zzUnpackRowMap("", i, arrayOfInt);
    return arrayOfInt;
  }

  private static int zzUnpackRowMap(String paramString, int paramInt, int[] paramArrayOfInt)
  {
    int i = 0;
    int j = paramInt;
    int k = paramString.length();
    while (i < k)
    {
      int m = paramString.charAt(i++) << '\020';
      paramArrayOfInt[(j++)] = (m | paramString.charAt(i++));
    }
    return j;
  }

  private static int[] zzUnpackAttribute()
  {
    int[] arrayOfInt = new int[45];
    int i = 0;
    i = zzUnpackAttribute("", i, arrayOfInt);
    return arrayOfInt;
  }

  private static int zzUnpackAttribute(String paramString, int paramInt, int[] paramArrayOfInt)
  {
    int i = 0;
    int j = paramInt;
    int k = paramString.length();
    while (i < k)
    {
      int m = paramString.charAt(i++);
      int n = paramString.charAt(i++);
      do
      {
        paramArrayOfInt[(j++)] = n;
        m--;
      }
      while (m > 0);
    }
    return j;
  }

  int getPosition()
  {
    return this.yychar;
  }

  Yylex(Reader paramReader)
  {
    this.zzReader = paramReader;
  }

  Yylex(InputStream paramInputStream)
  {
    this(new InputStreamReader(paramInputStream));
  }

  private static char[] zzUnpackCMap(String paramString)
  {
    char[] arrayOfChar = new char[65536];
    int i = 0;
    int j = 0;
    while (i < 90)
    {
      char k = paramString.charAt(i++);
      char m = paramString.charAt(i++);
      do
      {
        arrayOfChar[(j++)] = m;
        k--;
      }
      while (k > 0);
    }
    return arrayOfChar;
  }

  private boolean zzRefill()
    throws IOException
  {
    if (this.zzStartRead > 0)
    {
      System.arraycopy(this.zzBuffer, this.zzStartRead, this.zzBuffer, 0, this.zzEndRead - this.zzStartRead);
      this.zzEndRead -= this.zzStartRead;
      this.zzCurrentPos -= this.zzStartRead;
      this.zzMarkedPos -= this.zzStartRead;
      this.zzStartRead = 0;
    }
    if (this.zzCurrentPos >= this.zzBuffer.length)
    {
      char[] arrayOfChar = new char[this.zzCurrentPos * 2];
      System.arraycopy(this.zzBuffer, 0, arrayOfChar, 0, this.zzBuffer.length);
      this.zzBuffer = arrayOfChar;
    }
    int i = this.zzReader.read(this.zzBuffer, this.zzEndRead, this.zzBuffer.length - this.zzEndRead);
    if (i > 0)
    {
      this.zzEndRead += i;
      return false;
    }
    if (i == 0)
    {
      int j = this.zzReader.read();
      if (j == -1)
        return true;
      this.zzBuffer[(this.zzEndRead++)] = (char)j;
      return false;
    }
    return true;
  }

  public final void yyclose()
    throws IOException
  {
    this.zzAtEOF = true;
    this.zzEndRead = this.zzStartRead;
    if (this.zzReader != null)
      this.zzReader.close();
  }

  public final void yyreset(Reader paramReader)
  {
    this.zzReader = paramReader;
    this.zzAtBOL = true;
    this.zzAtEOF = false;
    this.zzEndRead = (this.zzStartRead = 0);
    this.zzCurrentPos = (this.zzMarkedPos = 0);
    this.yyline = (this.yychar = this.yycolumn = 0);
    this.zzLexicalState = 0;
  }

  public final int yystate()
  {
    return this.zzLexicalState;
  }

  public final void yybegin(int paramInt)
  {
    this.zzLexicalState = paramInt;
  }

  public final String yytext()
  {
    return new String(this.zzBuffer, this.zzStartRead, this.zzMarkedPos - this.zzStartRead);
  }

  public final char yycharat(int paramInt)
  {
    return this.zzBuffer[(this.zzStartRead + paramInt)];
  }

  public final int yylength()
  {
    return this.zzMarkedPos - this.zzStartRead;
  }

  private void zzScanError(int paramInt)
  {
    String str;
    try
    {
      str = ZZ_ERROR_MSG[paramInt];
    }
    catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
    {
      str = ZZ_ERROR_MSG[0];
    }
    throw new Error(str);
  }

  public void yypushback(int paramInt)
  {
    if (paramInt > yylength())
      zzScanError(2);
    this.zzMarkedPos -= paramInt;
  }

  public Yytoken yylex()
    throws IOException, ParseException
  {
    int n = this.zzEndRead;
    char[] arrayOfChar1 = this.zzBuffer;
    char[] arrayOfChar2 = ZZ_CMAP;
    int[] arrayOfInt1 = ZZ_TRANS;
    int[] arrayOfInt2 = ZZ_ROWMAP;
    int[] arrayOfInt3 = ZZ_ATTRIBUTE;
    while (true)
    {
      int m = this.zzMarkedPos;
      this.yychar += m - this.zzStartRead;
      int j = -1;
      int k = this.zzCurrentPos = this.zzStartRead = m;
      this.zzState = ZZ_LEXSTATE[this.zzLexicalState];
      int i;
      while (true)
      {
        if (k < n)
        {
          i = arrayOfChar1[(k++)];
        }
        else
        {
          if (this.zzAtEOF)
          {
            i = -1;
            break;
          }
          this.zzCurrentPos = k;
          this.zzMarkedPos = m;
          boolean bool = zzRefill();
          k = this.zzCurrentPos;
          m = this.zzMarkedPos;
          arrayOfChar1 = this.zzBuffer;
          n = this.zzEndRead;
          if (bool)
          {
            i = -1;
            break;
          }
          i = arrayOfChar1[(k++)];
        }
        int i1 = arrayOfInt1[(arrayOfInt2[this.zzState] + arrayOfChar2[i])];
        if (i1 == -1)
          break;
        this.zzState = i1;
        int i3 = arrayOfInt3[this.zzState];
        if ((i3 & 0x1) == 1)
        {
          j = this.zzState;
          m = k;
          if ((i3 & 0x8) == 8)
            break;
        }
      }
      this.zzMarkedPos = m;
      Object localObject;
      switch (j < 0 ? j : ZZ_ACTION[j])
      {
      case 11:
        this.sb.append(yytext());
      case 25:
        break;
      case 4:
        this.sb.delete(0, this.sb.length());
        yybegin(2);
      case 26:
        break;
      case 16:
        this.sb.append('\b');
      case 27:
        break;
      case 6:
        return new Yytoken(2, null);
      case 28:
        break;
      case 23:
        localObject = Boolean.valueOf(yytext());
        return new Yytoken(0, localObject);
      case 29:
        break;
      case 22:
        return new Yytoken(0, null);
      case 30:
        break;
      case 13:
        yybegin(0);
        return new Yytoken(0, this.sb.toString());
      case 31:
        break;
      case 12:
        this.sb.append('\\');
      case 32:
        break;
      case 21:
        localObject = Double.valueOf(yytext());
        return new Yytoken(0, localObject);
      case 33:
        break;
      case 1:
        throw new ParseException(this.yychar, 0, new Character(yycharat(0)));
      case 34:
        break;
      case 8:
        return new Yytoken(4, null);
      case 35:
        break;
      case 19:
        this.sb.append('\r');
      case 36:
        break;
      case 15:
        this.sb.append('/');
      case 37:
        break;
      case 10:
        return new Yytoken(6, null);
      case 38:
        break;
      case 14:
        this.sb.append('"');
      case 39:
        break;
      case 5:
        return new Yytoken(1, null);
      case 40:
        break;
      case 17:
        this.sb.append('\f');
      case 41:
        break;
      case 24:
        try
        {
          int i2 = Integer.parseInt(yytext().substring(2), 16);
          this.sb.append((char)i2);
        }
        catch (Exception localException)
        {
          throw new ParseException(this.yychar, 2, localException);
        }
      case 42:
        break;
      case 20:
        this.sb.append('\t');
      case 43:
        break;
      case 7:
        return new Yytoken(3, null);
      case 44:
        break;
      case 2:
        Long localLong = Long.valueOf(yytext());
        return new Yytoken(0, localLong);
      case 45:
        break;
      case 18:
        this.sb.append('\n');
      case 46:
        break;
      case 9:
        return new Yytoken(5, null);
      case 47:
        break;
      case 3:
      case 48:
        break;
      default:
        if ((i == -1) && (this.zzStartRead == this.zzCurrentPos))
        {
          this.zzAtEOF = true;
          return null;
        }
        zzScanError(1);
      }
    }
  }
}

/* Location:           /opt/workspace_vasja/DotMatrixPrinter/classes/
 * Qualified Name:     org.json.simple.parser.Yylex
 * JD-Core Version:    0.6.0
 */