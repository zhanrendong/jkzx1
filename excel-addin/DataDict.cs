using System;
using System.Collections.Generic;
using System.Linq;
using ExcelDna.Integration;
using RestSharp;
using System.Text.RegularExpressions;
using System.Runtime.Caching;

namespace QLAddin
{
    public static class DataDict
    {
        private static Regex arrayRegex = new Regex(@"\[(\d+)\]");
        private static ObjectCache cache = MemoryCache.Default;
        private static CacheItemPolicy policy = new CacheItemPolicy();
        private static string CreateGuid()
        {
            Guid guid = Guid.Empty;
            while (Guid.Empty == guid)
            {
                guid = Guid.NewGuid();
            }

            // Uses base64 encoding the guid.(Or  ASCII85 encoded)
            // But not recommend using Hex, as it is less efficient.
            return Convert.ToBase64String(guid.ToByteArray());
        }
        private static string genId(string name)
        {
            string id = CreateGuid();
            if (name == null)
                return "DataDict~" + id;
            else if (name == "DelayedJob")
                return name + "~" + id;
            else
                return name + ":DataDict~" + id;
        }

        public static string put(object o, string name = null)
        {
            string id = genId(name);
            cache.Set(id, o, policy);
            return id;
        }

        public static object get(string id)
        {
            return cache.Get(id);
        }

        private static object parseOneKey(object json, string key)
        {
            if (json is Dictionary<string, object>)
            {
                return ((Dictionary<string, object>)json)[key];
            }
            else if (json is JsonArray)
            {
                if (key.Equals("[]"))
                    return ((JsonArray)json).ToArray();
                Match match = arrayRegex.Match(key);
                if (match.Success)
                {
                    int idx = 0;
                    if (!int.TryParse(match.Groups[1].Value, out idx))
                        throw new Exception("Error: cannot get array index from the input key " + key);
                    return ((JsonArray)json).ToArray()[idx];
                }
                else
                {
                    throw new Exception("Error: the input is a json array, but the given key is not of array type [index]");
                }
            }
            else
                throw new Exception("Error: cannot recoganize data dict type when parsing key " + key);
        }

        private static object[] parseArrayKeys(object[] jsons, string key)
        {
            return jsons.Select(json => parseOneKey(json, key)).ToArray();
        }

        private static object getFromDataDictByPath(object json, string path)
        {
            string[] keys = path.Split('.');
            object o = json;
            foreach (string key in keys)
            {
                try
                {
                    if (o is object[])
                        o = parseArrayKeys((object[])o, key);
                    else
                        o = parseOneKey(o, key);
                }
                catch (KeyNotFoundException)
                {
                    return "Error: cannot find in DataDict key " + key + " in path " + path;
                }
                catch (Exception e)
                {
                    return e.Message;
                }           
            }
            if (o is JsonArray)
                return Utils.JsonArrayToArray2D(((JsonArray)o).ToArray());
            else if (o is Dictionary<string, object>)
                return Utils.JsonToExcelObj("", o);
            else
                return Utils.ParseJsonScalar(o);
        }

        [ExcelFunction(Description = "从数据字典中获取给定字段", Name = "exlExtract")]
        public static object getFromDataDict(string dict, string path)
        {
            object json = DataDict.get(dict);
            if (json == null)
                return "Error: cannot locate data dict object " + dict;
            object o = getFromDataDictByPath(json, path);
            if (o is object[])
            {
                object[,] ret = new object[((object[])o).Length, 1];
                for (int i = 0; i < ((object[])o).Length; ++i)
                {
                    ret[i, 0] = ((object[])o)[i];
                }
                return ret;
            }
            else
                return o;
        }

        [ExcelFunction(Description = "展示DataDict", Name = "exlDataDictShow")]
        public static object flattenDataDict(String dict)
        {
            object json = DataDict.get(dict);
            if (json == null)
                return "Error: cannot locate data dict object " + dict;
            return Utils.JsonToExcelObjWithoutTypeInfo(json);
        }
    }
}
