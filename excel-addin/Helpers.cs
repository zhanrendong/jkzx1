using System;
using System.Linq;
using ExcelDna.Integration;

namespace BCTAddin
{
    public static class Helpers
    {
        [ExcelFunction(Description = "将两行合并为一个Range", Name = "exlRangeCombineRows")]
        public static object[,] ExcelCombineRows(object[,] range1, object[,] range2)
        {
            int cols = Math.Max(range1.GetUpperBound(1), range2.GetUpperBound(1)) + 1;
            int rows = range1.GetUpperBound(0) + range2.GetUpperBound(0) + 2;
            object[,] ret = new object[rows, cols];
            for (int i = 0; i <= range1.GetUpperBound(0); ++i)
                for (int j = 0; j <= range1.GetUpperBound(1); ++j)
                    ret[i, j] = range1[i, j];
            for (int i = 0; i <= range2.GetUpperBound(0); ++i)
                for (int j = 0; j <= range2.GetUpperBound(1); ++j)
                    ret[range1.GetUpperBound(0) + 1 + i, j] = range2[i, j];
            return ret;
        }
        [ExcelFunction(Description = "将两列合并为一个Range", Name = "exlRangeCombineColumns")]
        public static object[,] ExcelCombineColumns(object[,] range1, object[,] range2)
        {
            int rows = Math.Max(range1.GetUpperBound(0), range2.GetUpperBound(0)) + 1;
            int cols = range1.GetUpperBound(1) + range2.GetUpperBound(1) + 2;
            object[,] ret = new object[rows, cols];
            for (int i = 0; i <= range1.GetUpperBound(0); ++i)
                for (int j = 0; j <= range1.GetUpperBound(1); ++j)
                    ret[i, j] = range1[i, j];
            for (int i = 0; i <= range2.GetUpperBound(0); ++i)
                for (int j = 0; j <= range2.GetUpperBound(1); ++j)
                    ret[i, range1.GetUpperBound(1) + 1 + j] = range2[i, j];
            return ret;
        }
        [ExcelFunction(Description = "将两列合并为一个Range", Name = "exlPick")]
        public static object[,] ExcelPick(object[,] data, object[,] picks)
        {
            int rows = picks.GetLength(0), cols = picks.GetLength(1);
            int n = rows > cols ? rows : cols;
            string[] required = new string[n];
            for (int i = 0; i < n; ++i)
                required[i] = rows > cols ? (string)picks[i, 0] : (string)picks[0, i];

            int inputRows = data.GetLength(0), inputCols = data.GetLength(1);
            string[] headers = new string[inputCols];
            for (int i = 0; i < inputCols; ++i)
            {
                headers[i] = (string)data[0, i];
            }
            var commonHeaders = required.Intersect(headers).ToArray();
            object[,] ret = new object[inputRows - 1, commonHeaders.Length];
            for (int i = 0; i < commonHeaders.Length; ++i)
            {
                int idx = Array.IndexOf(headers, commonHeaders[i]);
                for (int j = 0; j < inputRows - 1; ++j)
                    ret[j, i] = data[j + 1, idx];
            }
            return ret;
        }
    }
}
