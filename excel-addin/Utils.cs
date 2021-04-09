using ExcelDna.Integration;
using RestSharp;
using System;
using System.Linq;
using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace QLAddin
{
    public static class Utils
    {
        private static Regex timeRegex = new Regex(@"^\d{4}-\d{2}-\d{2}");

        public class User
        {
            public String token { get; set; }
            public List<String> roles { get; set; }
        };

        public class AuthToken
        {
            public User result { get; set; }
            public QLError error { get; set; }
            public int id { get; set; }
            public String jsonrpc { get; set; }
        }

        public class QLAPIParam
        {
            public string name { get; set; }
            public string type { get; set; }
            public string description { get; set; }
        }

        public class QLAPIInfo
        {
            public string method { get; set; }
            public string provider { get; set; }
            public string description { get; set; }
            public List<QLAPIParam> args { get; set; }
            public string retName { get; set; }
            public string retType { get; set; }
            public string retDescription { get; set; }
        }

        public class QLDelayedCall
        {
            public string body { get; set; }
            public string retType { get; set; }
        }

        public class QLError
        {
            public string message { get; set; }
            public int code { get; set; }
        }

        public class QLResult
        {
            public object result { get; set; }
            public QLError error { get; set; }
        }

        private static string ExcelArray1DToString(string type, object arrIn)
        {
            if (arrIn is ExcelMissing)
                return "[]";
            if (!(arrIn is object[,]))
            {
                return "[" + ExcelObjToString(type, arrIn) + "]";
            }
            object[,] arr = (object[,])arrIn;
            List<string> ret = new List<string>();
            int rows = arr.GetLength(0), cols = arr.GetLength(1);
            int n = rows > cols ? rows : cols;
            for (int i = 0; i < n; ++i)
            {
                object o = rows > cols ? arr[i, 0] : arr[0, i];
                if (o is ExcelError || o is ExcelMissing || o is ExcelEmpty)
                {
                    continue;
                }
                ret.Add(ExcelObjToString(type, o));
            }
            return "[" + String.Join(",", ret) + "]";
        }

        private static string ExcelArray2DToString(string type, object[,] v)
        {
            Dictionary<string, string> typeDict;
            try
            {
                typeDict = RestSharp.SimpleJson.DeserializeObject<Dictionary<string, string>>(type);
            }
            catch (Exception)
            {
                typeDict = new Dictionary<string, string>();
            }
            int rows = v.GetLength(0);
            int cols = v.GetUpperBound(1) + 1;
            List<string> pairs = new List<string>();
            for (int i = 0; i < rows; ++i)
            {
                object o = v[i, 0];
                if (o is ExcelError || o is ExcelMissing || o is ExcelEmpty)
                {
                    continue;
                }
                string key = (string)o;
                string argType;
                bool withType = typeDict.TryGetValue(key, out argType);
                o = v[i, 1];
                if (o is ExcelError || o is ExcelMissing || o is ExcelEmpty)
                {
                    continue;
                }
                if (cols == 2 || (cols > 2 && (v[i, 2] is ExcelError || v[i, 2] is ExcelMissing || v[i, 2] is ExcelEmpty)))
                {
                    string value = withType ? ExcelObjToString(argType, o) : ExcelObjToStringWithoutType(o);
                    pairs.Add("\"" + key + "\":" + value);
                } else
                {
                    List<string> ar = new List<string>();
                    for (int j = 1; j < cols; ++j)
                    {
                        if (o is ExcelError || o is ExcelMissing || o is ExcelEmpty)
                            break;
                        o = v[i, j];
                        ar.Add(withType ? ExcelObjToString(argType, o) : ExcelObjToStringWithoutType(o));
                    }
                    pairs.Add("\"" + key + "\":" + "[" + String.Join(",", ar) + "]");
                }
                
            }
            if (pairs.Count == 0)
                return null;
            else
                return "{" + String.Join(",", pairs) + "}";
        }

        private static string ExcelArray2DToMatrix(object[,] v)
        {
            string ret = "[";
            for (int i = 0; i <= v.GetUpperBound(0); ++i)
            {
                if (v[i, 0] is ExcelError || v[i, 0] is ExcelMissing || v[i, 0] is ExcelEmpty)
                    continue;
                string row = "[";
                for (int j = 0; j <= v.GetUpperBound(1); ++j)
                {
                    if (v[i, j] is ExcelError || v[i, j] is ExcelMissing || v[i, j] is ExcelEmpty)
                        continue;
                    row += ((j == 0 ? "" : ",") + v[i, j]);
                }
                row += "]";
                ret += ((i == 0 ? "" : ",") + row);
            }
            ret += "]";
            return ret;
        }

        private static string ExcelTableToJson(object[,] table)
        {
            int numRows = table.GetUpperBound(0) + 1;
            // get rid of rows with missing values
            for (int i = 0; i < numRows; ++i)
            {
                if (table[i, 0] is ExcelMissing || table[i, 0] is ExcelEmpty || table[i, 0] is ExcelError)
                {
                    numRows = i;
                    break;
                }
            }
            int numCols = table.GetUpperBound(1) + 1;
            // get rid of columns with missing values
            for (int i = 0; i < numCols; ++i)
            {
                if (table[0, i] is ExcelMissing || table[0, i] is ExcelEmpty || table[0, i] is ExcelError)
                {
                    numCols = i;
                    break;
                }
            }
            string[] fields = new string[numCols + 1];
            // get headers
            string[] headers = new string[numCols];
            for (int i = 0; i < numCols; ++i)
            {
                headers[i] = "\"" + (string)table[0, i] + "\"";
                string[] values = new string[numRows - 1];
                for (int j = 1; j < numRows; ++j)
                {
                    values[j - 1] = ExcelObjToStringWithoutType(table[j, i]);
                }
                fields[i + 1] = "\"" + (string)table[0, i] + "\":[" + String.Join(",", values) + "]";
            }
            fields[0] = "\"display_order\":" + "[" + String.Join(",", headers) + "]";
            return "{" + String.Join(",", fields) + "}";
        }

        private static string ExcelTableToJsonArray(object[,] table)
        {
            int numRows = table.GetUpperBound(0) + 1;
            // get rid of rows with missing values
            for (int i = 0; i < numRows; ++i)
            {
                if (table[i, 0] is ExcelMissing || table[i, 0] is ExcelEmpty || table[i, 0] is ExcelError)
                {
                    numRows = i;
                    break;
                }
            }
            int numCols = table.GetUpperBound(1) + 1;
            // get rid of columns with missing values
            for (int i = 0; i < numCols; ++i)
            {
                if (table[0, i] is ExcelMissing || table[0, i] is ExcelEmpty || table[0, i] is ExcelError)
                {
                    numCols = i;
                    break;
                }
            }
            string[] fields = new string[numCols + 1];
            // get headers
            string[] headers = new string[numCols];
            for (int i = 0; i < numCols; ++i)
            {
                headers[i] = (string)table[0, i];
            }
            // go through each row and create json
            string[] rows = new string[numRows - 1];
            for (int i = 1; i < numRows; ++i)
            {
                string[] values = new string[numCols];
                for (int j = 0; j < numCols; ++j)
                {
                    values[j] = "\"" + headers[j] + "\":" + ExcelObjToStringWithoutType(table[i, j]);
                }
                rows[i - 1] = "{" + String.Join(",", values) + "}";
            }
            return "[" + String.Join(",", rows) + "]";
        }

        public static object[,] JsonArrayToArray2D(object[] o)
        {
            // get headers
            List<string> headers = new List<string>();
            int n = o.Length;
            for (int i = 0; i < n; ++i)
            {
                headers = headers.Union(((Dictionary<string, object>)o[i]).Keys).ToList();
            }
            object[,] ret = new object[n + 1, headers.Count];
            for (int i = 0; i < headers.Count; ++i)
                ret[0, i] = headers[i];
            for (int i = 0; i < n; ++i)
            {
                for (int j = 0; j < headers.Count; ++j)
                {
                    ((Dictionary<string, object>)o[i]).TryGetValue(headers[j], out ret[i + 1, j]);
                }
            }
            return ret;
        }

        public static object JsonToExcelObj(string type, object o)
        {
            if (type.Equals("DateTime"))
            {
                DateTime d = DateTime.Parse((string)o, null, System.Globalization.DateTimeStyles.RoundtripKind);
                return d.ToOADate();
            }
            else if (type.Equals("ArrayDouble") || type.Equals("ArrayBoolean") || type.Equals("ArrayString"))
            {
                object[] a = ((JsonArray)o).ToArray();
                int n = a.Length;
                if (n == 1)
                    return a[0];
                object[,] ret = new object[n, 1];
                for (int i = 0; i < n; ++i)
                    ret[i, 0] = a[i];
                return ret;
            }
            else if (type.Equals("ArrayDateTime"))
            {
                object[] a = ((JsonArray)o).ToArray();
                int n = a.Length;
                double[,] ret = new double[n, 1];
                for (int i = 0; i < n; ++i)
                    ret[i, 0] = DateTime.Parse((string)a[i], null, System.Globalization.DateTimeStyles.RoundtripKind).ToOADate();
                if (n == 1)
                    return ret[0, 0];
                return ret;
            }
            else if (type.Equals("Matrix"))
            {
                object[] a = ((JsonArray)o).ToArray();
                int n = a.Length;
                int m = ((JsonArray)a[0]).Count;
                double[,] ret = new double[n, m];
                for (int i = 0; i < n; ++i)
                {
                    object[] row = ((JsonArray)a[i]).ToArray();
                    for (int j = 0; j < m; ++j)
                    {
                        ret[i, j] = (double)row[j];
                    }
                }
                return ret;
            }
            else if (type.Equals("Table"))
            {
                if (o is JsonArray)
                    return JsonArrayToArray2D(((JsonArray)o).ToArray());
                Dictionary<string, object> table = (Dictionary<string, object>)o;
                object[,] ret = null;
                object display;
                if (table.TryGetValue("display_order", out display))
                {
                    object[] headers = ((JsonArray)display).ToArray();
                    int cols = headers.Length, rows = 0;
                    bool rowsKnown = false;
                    for (int i = 0; i < cols; ++i)
                    {
                        string header = (string)headers[i];
                        object c;
                        if (table.TryGetValue(header, out c))
                        {
                            if (!rowsKnown)
                            {
                                rows = ((JsonArray)c).ToArray().Length;
                                ret = new object[rows, cols];
                                rowsKnown = true;
                            }
                            for (int j = 0; j < rows; ++j)
                            {
                                ret[j, i] = ParseJsonScalar(((JsonArray)c).ToArray()[j]);
                            }
                        }
                    }
                }
                return ret;
            }
            else
                return o;
        }

        public static object[,] JsonToExcelObj(string type, Dictionary<string, object> dict)
        {
            Dictionary<string, string> typeDict = null;
            try
            {
                typeDict = RestSharp.SimpleJson.DeserializeObject<Dictionary<string, string>>(type);
            }
            catch (Exception)
            {
                return null;
            }

            int n = dict.Count;
            object[,] ret = new object[n, 2];
            int i = 0;
            foreach (KeyValuePair<string, object> p in dict)
            {
                ret[i, 0] = p.Key;
                // special handling of datetime
                //   this is necessary because excel datetime is a double
                if (typeDict[p.Key].Equals("DateTime"))
                {
                    DateTime d = DateTime.Parse((string)p.Value, null, System.Globalization.DateTimeStyles.RoundtripKind);
                    ret[i, 1] = d.ToOADate();
                }
                else
                    ret[i, 1] = p.Value;
                ++i;
            }
            return ret;
        }

        private static void ParseJsonArray(string prefix, List<object> dict, List<object[]> soFar)
        {
            int n = dict.Count;
            for (int i = 0; i < n; ++i)
            {
                string newPrefix = prefix + "." + i;
                if (dict[i] is List<object>)
                {
                    ParseJsonArray(newPrefix, (List<object>)dict[i], soFar);
                }
                else if (dict[i] is Dictionary<string, object>)
                {
                    ParseJsonDict(newPrefix, (Dictionary<string, object>)dict[i], soFar);
                }
                else
                {
                    // scalar
                    object[] row = new object[2];
                    row[0] = newPrefix;
                    if (dict[i] is string && timeRegex.Match((string)dict[i]).Success)
                    {
                        DateTime d = DateTime.Parse((string)dict[i], null, System.Globalization.DateTimeStyles.RoundtripKind);
                        row[1] = d.ToOADate();
                    }
                    else
                    {
                        row[1] = dict[i];
                    }
                    soFar.Add(row);
                }
            }
        }

        private static void ParseJsonDict(string prefix, Dictionary<string, object> dict, List<object[]> soFar)
        {
            foreach (KeyValuePair<string, object> p in dict)
            {
                object[] row = new object[2];
                if (prefix.Length == 0)
                    row[0] = p.Key;
                else
                    row[0] = prefix + "." + p.Key;
                // special handling of datetime
                //   this is necessary because excel datetime is a double
                if (p.Value is string && timeRegex.Match((string)p.Value).Success)
                {
                    DateTime d = DateTime.Parse((string)p.Value, null, System.Globalization.DateTimeStyles.RoundtripKind);
                    row[1] = d.ToOADate();
                    soFar.Add(row);
                }
                else if (p.Value is List<object>)
                {
                    ParseJsonArray((string)row[0], (List<object>)p.Value, soFar);
                }
                else if (p.Value is Dictionary<String, object>)
                {
                    ParseJsonDict((string)row[0], (Dictionary<string, object>)p.Value, soFar);
                }
                else
                {
                    row[1] = p.Value;
                    soFar.Add(row);
                }
            }
        }

        public static object ParseJsonScalar(object o)
        {
            if (o is string && timeRegex.Match((string)o).Success)
            {
                DateTime d = DateTime.Parse((string)o, null, System.Globalization.DateTimeStyles.RoundtripKind);
                return d.ToOADate();
            }
            else
            {
                return o;
            }
        }

        public static object[,] JsonToExcelObjWithoutTypeInfo(object dict)
        {
            List<object[]> result = new List<object[]>();
            if (dict is Dictionary<String, object>)
                ParseJsonDict("", (Dictionary<String, object>)dict, result);
            else
                ParseJsonArray("", (List<object>)dict, result);
            int n = result.Count;
            object[,] ret = new object[n, 2];
            for (int i = 0; i < n; ++i)
            {
                ret[i, 0] = result[i][0];
                ret[i, 1] = result[i][1];
            }
            return ret;
        }

        public static string ExcelObjToString(string type, object o)
        {
            switch (type)
            {
                case "String":
                    if (o is string)
                        return "\"" + ((string)o).Replace("\"", "\\\"") + "\"";
                    else
                        return "\"" + (o.ToString()).Replace("\"", "\\\"") + "\"";
                case "Enum":
                case "Handle":
                    return "\"" + (string)o + "\"";
                case "Boolean":
                case "boolean":
                    return ((bool)o) ? "true" : "false";
                case "Double":
                case "double":
                    return ((double)o).ToString();
                case "Integer":
                    return ((int)((double)o + 0.5)).ToString();
                case "Long":
                    return ((long)((double)o + 0.5)).ToString();
                case "DateTime":
                    double d = (double)o;
                    if (Math.Abs(d % 1) <= (Double.Epsilon * 100))
                        return "\"" + DateTime.FromOADate(d).ToString("yyyy-MM-dd") + "\"";
                    else
                        return "\"" + DateTime.FromOADate(d).ToString("s", System.Globalization.CultureInfo.InvariantCulture) + "\"";
                case "ArrayString":
                    return ExcelArray1DToString("String", o);
                case "ArrayDateTime":
                    return ExcelArray1DToString("DateTime", o);
                case "ArrayDouble":
                    return ExcelArray1DToString("Double", o);
                case "ArrayBoolean":
                    return ExcelArray1DToString("Boolean", o);
                case "ArrayHandle":
                    return ExcelArray1DToString("Handle", o);
                case "ArrayEnum":
                    return ExcelArray1DToString("Enum", o);
                case "Matrix":
                    return ExcelArray2DToMatrix((object[,])o);
                case "Table":
                    return ExcelTableToJson((object[,])o);
                case "JsonArray":
                    return ExcelTableToJsonArray((object[,])o);
                default:
                    if (o is object[,])
                        return ExcelArray2DToString(type, (object[,])o);
                    else
                        return null;
            }
        }

        public static string ExcelObjToStringWithoutType(object o)
        {
            if (o is double)
            {
                return ((Double)o).ToString();
            }
            else if (o is string)
            {
                return "\"" + (string)o + "\"";
            }
            else if (o is bool)
            {
                return ((bool)o) ? "true" : "false";
            }
            else
            {
                return null;
            }
        }
    }
}
