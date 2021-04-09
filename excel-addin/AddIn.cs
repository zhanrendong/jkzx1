using System;
using System.Collections.Generic;
using RestSharp;
using ExcelDna.Integration;
using System.Windows.Forms;
using System.Net;

namespace QLAddin
{
    public static class Addin
    {
        private static BctAddin.Settings settings = new BctAddin.Settings();

        private static object CallAPI(Utils.QLAPIInfo info, List<object> inputs)
        {
            if (ExcelDnaUtil.IsInFunctionWizard())
            {
                if (settings.display.Equals("chinese"))
                    return "等待输入...";
                else
                    return "Waiting for inputs ...";
            }

            List<Utils.QLAPIParam> argInfo = info.args;
            string api = info.method;

            bool delayed = false;
            List<string> entries = new List<string>();
            for (int i = 0; i < argInfo.Count; ++i)
            {
                if (inputs[i] is ExcelMissing)
                    continue;
                if (argInfo[i].name == "delayed")
                {
                    delayed = (Boolean) inputs[i];
                    continue;
                }
                try
                {
                    entries.Add("\"" + argInfo[i].name + "\":" + Utils.ExcelObjToString(argInfo[i].type, inputs[i]));
                }
                catch (Exception e)
                {
                    return "Error while converting " + (i + 1).ToString() + "th input to Excel object with message: " + e.Message;
                }

            }
            string args = "{" + String.Join(",", entries) + "}";
            string body = "{\"jsonrpc\":\"2.0\",\"id\":\"100\",\"method\":\"" + api + "\",\"params\":" + args + "}";

            var request = new RestRequest(info.provider + "/rpc", Method.POST);
            request.RequestFormat = DataFormat.Json;
            request.AddParameter("Application/Json", body, ParameterType.RequestBody);
            //add header(for trade-service)
            request.AddHeader("Authorization", "Bearer " + settings.token);
            if (delayed)
            {
                return DataDict.put(new Utils.QLDelayedCall { body = body, retType = info.retType}, "DelayedJob");
            }
            var response = settings.client.Execute<Utils.QLResult>(request);
            if (response.ErrorException != null)
            {
                return "Exception while executing the API: " + response.ErrorException.Message;
            }
            if ((int)response.StatusCode != 200)
            {
                return "Error: Server returned error: " + response.Data.error.message;
            }
            else
            {
                if (response.Data.result == null)
                {
                    if (response.Data.error != null)
                        return "Error: Server returned error: " + response.Data.error.message;
                    else
                        return ExcelError.ExcelErrorNull;
                }
                // special data type: a DataDict is just a json object
                // we put it into a cache and then the user can query it for values by providing keys or maybe query criterion
                if (info.retType.Equals("DataDict"))
                    return DataDict.put(response.Data.result, api);
                if (info.retType.Equals("Table")) // handling of table is different from all other types
                    return Utils.JsonToExcelObj(info.retType, response.Data.result);
                if (info.retType.Equals("Json") || response.Data.result is Dictionary<string, object>)
                {
                    if (response.Data.result is List<object>)
                    {
                        List<object> arr = new List<object>();
                        bool isNested = false;
                        foreach (object o in (List<object>)response.Data.result)
                        {
                            if (!(o is List<object> || o is Dictionary<string, object>))
                                arr.Add(Utils.ParseJsonScalar(o));
                            else
                            {
                                isNested = true;
                                break;
                            }
                        }
                        if (isNested)
                            return Utils.JsonToExcelObjWithoutTypeInfo(response.Data.result);
                        else
                        {
                            if (arr.Count == 1)
                                return arr[0];
                            object[,] ret = new object[arr.Count, 1];
                            for (int i = 0; i < arr.Count; ++i)
                            {
                                ret[i, 0] = arr[i];
                            }
                            return ret;
                        }
                    }
                    else if (response.Data.result is Dictionary<string, object>)
                        return Utils.JsonToExcelObjWithoutTypeInfo(response.Data.result);
                    else
                        return Utils.ParseJsonScalar(response.Data.result);
                }
                else
                    return Utils.JsonToExcelObj(info.retType, response.Data.result);
            }
        }

        [ExcelFunction(Description = "向服务器端发送并行指令", Name = "exlExecParallel")]
        public static object ExecParallel(object[,] tasks)
        {
            List<string> entries = new List<string>();
            int rows = tasks.GetLength(0), cols = tasks.GetLength(1);
            
            List<string> retTypes = new List<string>();
            for (int i = 0; i < rows; ++i)
            {
                for (int j = 0; j < cols; ++j)
                {
                    object task = tasks[i, j];
                    if (task is ExcelError || task is ExcelMissing || task is ExcelEmpty)
                    {
                        continue;
                    }
                    String id = (String)(tasks[i, j]);
                    Utils.QLDelayedCall delayed = (Utils.QLDelayedCall)DataDict.get(id);
                    entries.Add(delayed.body);
                    retTypes.Add(delayed.retType);
                }
            }
            String json = "[" + String.Join(",", entries) + "]";
            var request = new RestRequest("excel-service/parallel", Method.POST);
            request.RequestFormat = DataFormat.Json;
            request.AddParameter("Application/Json", json, ParameterType.RequestBody);
            request.AddHeader("Authorization", "Bearer " + settings.token);
            var response = settings.client.Execute<Utils.QLResult>(request);
            if (response.ErrorException != null)
            {
                return "Exception while executing the API: " + response.ErrorException.Message;
            }
            List<object> results = (List<object>)response.Data.result;
            if ((int)response.StatusCode != 200 || results.Count < 1 )
            {
                return "Error: Parallel jobs failed.";
            }
            
            object[,] ret = new object[rows, cols];
            int count = 0;
            for(int i = 0; i < rows; ++i)
            {
                for (int j = 0; j < cols; ++j)
                {
                    object task = tasks[i, j];
                    if (task is ExcelError || task is ExcelMissing || task is ExcelEmpty)
                    {
                        continue;
                    }
                    Dictionary<string, object> r = (Dictionary<string, object>)results[count];
                    if (r.ContainsKey("error"))
                    {
                        ret[i, 0] = ((Dictionary<string, object>)r["error"])["message"];
                    }
                    else
                    {
                        object o = r["result"];
                        if (o is Dictionary<string, object> || o is List<object>)
                            ret[i, j] = DataDict.put(o, "DelayedJobResult");
                        else
                            ret[i, j] = Utils.JsonToExcelObj(retTypes[i], o);
                    }
                    count++;
                }
            }
            return ret;
        }

        private static bool registerOneFunction(string method)
        {
            var request = new RestRequest("excel-service/info/" + method, Method.GET);
            request.AddHeader("Authorization", "Bearer " + settings.token);
            var response = settings.client.Execute<Utils.QLAPIInfo>(request);
            Utils.QLAPIInfo info = response.Data;
            if (info == null)
                return false;
            info.provider = "excel-service";
            var apiAttr = new ExcelFunctionAttribute
            {
                Name = info.method,
                Category = "BCT Excel Addin",
                Description = info.description
            };
            List<Utils.QLAPIParam> args = info.args;
            List<object> argAttrs = new List<object>();
            foreach (var arg in args)
            {
                var attr = new ExcelArgumentAttribute
                {
                    Name = arg.name,
                    Description = arg.description
                };
                argAttrs.Add(attr);
            }
            Delegate func = null;
            switch (args.Count)
            {
                case 0:
                    func = MakeExcelUDFArgs0(info);
                    break;
                case 1:
                    func = MakeExcelUDFArgs1(info);
                    break;
                case 2:
                    func = MakeExcelUDFArgs2(info);
                    break;
                case 3:
                    func = MakeExcelUDFArgs3(info);
                    break;
                case 4:
                    func = MakeExcelUDFArgs4(info);
                    break;
                case 5:
                    func = MakeExcelUDFArgs5(info);
                    break;
                case 6:
                    func = MakeExcelUDFArgs6(info);
                    break;
                case 7:
                    func = MakeExcelUDFArgs7(info);
                    break;
                case 8:
                    func = MakeExcelUDFArgs8(info);
                    break;
                case 9:
                    func = MakeExcelUDFArgs9(info);
                    break;
                case 10:
                    func = MakeExcelUDFArgs10(info);
                    break;
                case 11:
                    func = MakeExcelUDFArgs11(info);
                    break;
                case 12:
                    func = MakeExcelUDFArgs12(info);
                    break;
                case 13:
                    func = MakeExcelUDFArgs13(info);
                    break;
                case 14:
                    func = MakeExcelUDFArgs14(info);
                    break;
                case 15:
                    func = MakeExcelUDFArgs15(info);
                    break;
                case 16:
                    func = MakeExcelUDFArgs16(info);
                    break;
                case 17:
                    func = MakeExcelUDFArgs17(info);
                    break;
                case 18:
                    func = MakeExcelUDFArgs18(info);
                    break;
                case 19:
                    func = MakeExcelUDFArgs19(info);
                    break;
                case 20:
                    func = MakeExcelUDFArgs20(info);
                    break;
                case 21:
                    func = MakeExcelUDFArgs21(info);
                    break;
            }
            if (func != null)
            {
                ExcelIntegration.RegisterDelegates(new List<Delegate> { func },
                   new List<object> { apiAttr },
                   new List<List<object>> { argAttrs });
                return true;
            }
            return false;
        }

        private static List<String> getApis(String service)
        {
            var request = new RestRequest(service + "/list", Method.GET);
            request.AddHeader("Authorization", "Bearer " + settings.token);
            var response = settings.client.Execute<List<string>>(request);
            if (response.ResponseStatus == ResponseStatus.Error)
            {
                MessageBox.Show("Unable to connect to the server. APIs not loaded.");
                return null;
            }
            return response.Data;
        }

        public class TestAddin : IExcelAddIn
        {
            public void AutoOpen()
            {
                ServicePointManager.ServerCertificateValidationCallback +=
                    (sender, certificate, chain, sslPolicyErrors) => true;
                string ip = settings.ip;
                int port = settings.port;
                string user = "guest";
                string pass = "";
                int auth_port = settings.port;
                bool useHttps = false;
                ShowInputDialog(ref ip, ref port, ref user, ref pass, ref useHttps);
                settings.Update(ip, port, "chinese", useHttps);
                // login with user/pass and get the token
                string token = "";
                if (user.Length != 0)
                {
                    List<string> entries = new List<string>();
                    entries.Add("\"username\":\"" + user + "\"");
                    entries.Add("\"password\":\"" + pass + "\"");
                    string body = "{" + String.Join(",", entries) + "}";
                    var loginRequest = new RestRequest("auth-service/users/login", Method.POST);
                    loginRequest.RequestFormat = DataFormat.Json;
                    loginRequest.AddParameter("Application/Json", body, ParameterType.RequestBody);
                    var loginResponse = settings.client.Execute<Utils.AuthToken>(loginRequest);
                    Utils.AuthToken res = loginResponse.Data;
                    if ((int)loginResponse.StatusCode != 200 || res.result == null)
                    {
                        MessageBox.Show("登录失败");
                        return;
                    }
                    token = res.result.token;
                }
                settings.token = token;
                List<String> apis = new List<string>();
                String[] services =
                {
                    "excel-service"
                };
                for (int i = 0; i < services.Length; ++i)
                {
                    List<String> methods = getApis(services[i]);
                    if (methods != null)
                        apis.AddRange(methods);
                }

                if (apis.Count == 0)
                {
                    MessageBox.Show("未发现任何可加载API");
                    return;
                }

                int count = 0;
                foreach (string method in apis)
                {
                    try
                    {
                        bool success = registerOneFunction(method);
                        if (success)
                        {
                            count++;
                        }
                    }
                    catch
                    {
                        continue;
                    }
                }
                MessageBox.Show("成功加载 " + count.ToString() + " APIs");
            }

            public void AutoClose() { }
        }

        /*
        [ExcelFunction(Description = "Test Excel object to Json string")]
        public static object TestExcelObjToJson(string type, object o)
        {
            return Utils.ExcelObjToString(type, o);
        }
        */
        /*
        [ExcelFunction(Description = "Test string to OADate")]
        public static object TestString2OADate(string t)
        {
            DateTime d = DateTime.Parse(t, null, System.Globalization.DateTimeStyles.RoundtripKind);
            return d.ToOADate();
        }
        */

        private static Func<object> MakeExcelUDFArgs0(Utils.QLAPIInfo info)
        {
            return () =>
            {
                return CallAPI(info, new List<object>());
            };
        }

        private static Func<object, object> MakeExcelUDFArgs1(Utils.QLAPIInfo info)
        {
            return (o1) =>
            {
                return CallAPI(info, new List<object> { o1 });
            };
        }

        private static Func<object, object, object> MakeExcelUDFArgs2(Utils.QLAPIInfo info)
        {
            return (o1, o2) =>
            {
                return CallAPI(info, new List<object> { o1, o2 });
            };
        }

        private static Func<object, object, object, object> MakeExcelUDFArgs3(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3 });
            };
        }

        private static Func<object, object, object, object, object> MakeExcelUDFArgs4(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4 });
            };
        }

        private static Func<object, object, object, object, object, object> MakeExcelUDFArgs5(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5 });
            };
        }

        private static Func<object, object, object, object, object, object, object> MakeExcelUDFArgs6(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6 });
            };
        }

        private static Func<object, object, object, object, object, object, object, object> MakeExcelUDFArgs7(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7 });
            };
        }

        private static Func<object, object, object, object, object, object, object, object, object> MakeExcelUDFArgs8(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7, o8) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7, o8 });
            };
        }

        private static Func<object, object, object, object, object, object, object, object, object, object> MakeExcelUDFArgs9(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7, o8, o9) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7, o8, o9 });
            };
        }

        private static Func<object, object, object, object, object, object, object, object, object, object, object> MakeExcelUDFArgs10(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7, o8, o9, o10) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7, o8, o9, o10 });
            };
        }

        private static Func<object, object, object, object, object, object, object, object, object, object, object, object> MakeExcelUDFArgs11(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11 });
            };
        }

        private static Func<object, object, object, object, object, object, object, object, object, object, object, object, object> MakeExcelUDFArgs12(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12 });
            };
        }

        private static Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object> MakeExcelUDFArgs13(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13 });
            };
        }

        private static Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object> MakeExcelUDFArgs14(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14 });
            };
        }

        private static Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object> MakeExcelUDFArgs15(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15 });
            };
        }

        private static Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object> MakeExcelUDFArgs16(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15, o16) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15, o16 });
            };
        }

        private delegate R Func<P1, P2, P3, P4, P5, P6, P7, P8, P9,P10, P11, P12, P13, P14, P15, P16, P17, R> (
                P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9,P10 p10, P11 p11, P12 p12, P13 p13, P14 p14,P15 p15, P16 p16, P17 p17);

        private static Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object> MakeExcelUDFArgs17(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15, o16, o17) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15, o16, o17 });
            };
        }

        private delegate R Func<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, R>(
                P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13, P14 p14, P15 p15, P16 p16, P17 p17, P18 p18);

        private static Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object> MakeExcelUDFArgs18(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15, o16, o17, o18) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15, o16, o17, o18 });
            };
        }

        private delegate R Func<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, R>(
                P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13, P14 p14, P15 p15, P16 p16, P17 p17, P18 p18, P19 p19);

        private static Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object> MakeExcelUDFArgs19(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15, o16, o17, o18, o19) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15, o16, o17, o18, o19 });
            };
        }

        private delegate R Func<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, R>(
                P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13, P14 p14, P15 p15, P16 p16, P17 p17, P18 p18, P19 p19, P20 p20);

        private static Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object> MakeExcelUDFArgs20(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15, o16, o17, o18, o19, o20) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15, o16, o17, o18, o19, o20 });
            };
        }

        private delegate R Func<P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, R>(
                P1 p1, P2 p2, P3 p3, P4 p4, P5 p5, P6 p6, P7 p7, P8 p8, P9 p9, P10 p10, P11 p11, P12 p12, P13 p13, P14 p14, P15 p15, P16 p16, P17 p17, P18 p18, P19 p19, P20 p20, P21 p21);

        private static Func<object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object, object> MakeExcelUDFArgs21(Utils.QLAPIInfo info)
        {
            return (o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15, o16, o17, o18, o19, o20, o21) =>
            {
                return CallAPI(info, new List<object> { o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15, o16, o17, o18, o19, o20, o21 });
            };
        }

        private static DialogResult ShowInputDialog(ref string ip, ref int port, ref string user, ref string pass, ref bool useHttps)
        {
            System.Drawing.Size size = new System.Drawing.Size(300, 400);
            Form inputBox = new Form();

            inputBox.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            inputBox.ClientSize = size;
            inputBox.Text = "Name";

            // whether use https
            Label httpsLabel = new Label();
            httpsLabel.Size = new System.Drawing.Size(100 - 10, 23);
            httpsLabel.Location = new System.Drawing.Point(5, 20);
            httpsLabel.Text = "使用https";
            httpsLabel.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
            inputBox.Controls.Add(httpsLabel);

            CheckBox https = new CheckBox();
            https.Checked = false;
            https.Location = new System.Drawing.Point(100, 20);
            inputBox.Controls.Add(https);
            // server ip input
            Label ipLabel = new Label();
            ipLabel.Size = new System.Drawing.Size(100 - 10, 23);
            ipLabel.Location = new System.Drawing.Point(5, 60);
            ipLabel.Text = "服务器地址:";
            ipLabel.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
            inputBox.Controls.Add(ipLabel);

            TextBox ipBox = new TextBox();
            ipBox.Size = new System.Drawing.Size(200 - 10, 23);
            ipBox.Location = new System.Drawing.Point(100, 60);
            ipBox.Text = ip;
            inputBox.Controls.Add(ipBox);

            // server port input
            Label portLabel = new Label();
            portLabel.Size = new System.Drawing.Size(100 - 10, 23);
            portLabel.Location = new System.Drawing.Point(5, 100);
            portLabel.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
            portLabel.Text = "端口:";
            inputBox.Controls.Add(portLabel);

            TextBox portBox = new TextBox();
            portBox.Size = new System.Drawing.Size(200 - 10, 23);
            portBox.Location = new System.Drawing.Point(100, 100);
            portBox.Text = port.ToString();
            portBox.Enabled = !https.Checked;
            inputBox.Controls.Add(portBox);

            https.CheckedChanged += new EventHandler((s, e) => {
                portBox.Enabled = !https.Checked;
            });

            // user name input
            Label userLabel = new Label();
            userLabel.Size = new System.Drawing.Size(100 - 10, 23);
            userLabel.Location = new System.Drawing.Point(5, 140);
            userLabel.Text = "用户名:";
            userLabel.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
            inputBox.Controls.Add(userLabel);

            TextBox userBox = new TextBox();
            userBox.Size = new System.Drawing.Size(200 - 10, 23);
            userBox.Location = new System.Drawing.Point(100, 140);
            userBox.Text = user;
            inputBox.Controls.Add(userBox);
            // password input
            Label passLabel = new Label();
            passLabel.Size = new System.Drawing.Size(100 - 10, 23);
            passLabel.Location = new System.Drawing.Point(5, 180);
            passLabel.Text = "密码:";
            passLabel.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
            inputBox.Controls.Add(passLabel);

            TextBox passBox = new TextBox();
            passBox.Size = new System.Drawing.Size(200 - 10, 23);
            passBox.Location = new System.Drawing.Point(100, 180);
            passBox.PasswordChar = '*';
            passBox.Text = pass;
            inputBox.Controls.Add(passBox);
            Button okButton = new Button();
            okButton.DialogResult = System.Windows.Forms.DialogResult.OK;
            okButton.Name = "okButton";
            okButton.Size = new System.Drawing.Size(75, 23);
            okButton.Text = "&OK";
            okButton.Location = new System.Drawing.Point(150 - 80, 360);
            inputBox.Controls.Add(okButton);

            Button cancelButton = new Button();
            cancelButton.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            cancelButton.Name = "cancelButton";
            cancelButton.Size = new System.Drawing.Size(75, 23);
            cancelButton.Text = "&Cancel";
            cancelButton.Location = new System.Drawing.Point(155, 360);
            inputBox.Controls.Add(cancelButton);

            inputBox.AcceptButton = okButton;
            inputBox.CancelButton = cancelButton;
            inputBox.StartPosition = FormStartPosition.CenterParent;

            DialogResult result = inputBox.ShowDialog();
            ip = ipBox.Text;
            port = Int32.Parse(portBox.Text);
            user = userBox.Text;
            pass = passBox.Text;
            useHttps = https.Checked;
            return result;
        }
    }
}