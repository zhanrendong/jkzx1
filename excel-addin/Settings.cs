using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using RestSharp;

namespace BctAddin
{
    class Settings
    {
        public Settings()
        {
            ip = "127.0.0.1";
            zone = "+08:00[Asia/Shanghai]";
            port = 16016;
            display = "chinese";
            client = new RestClient("http://" + ip + ":" + port.ToString());
            client.Timeout = 10000;
            token = "";
            https = false;
        }
        public void Update(string ip, int port, string display, bool https)
        {
            this.ip = ip;
            this.port = port;
            this.https = https;
            string uri = https ? "https://" + this.ip : "http://" + this.ip + ":" + this.port.ToString();
            client.BaseUrl = new Uri(uri);
            this.display = display;
        }
        public string ip { get; set; }
        public string zone { get; set; }
        public string display { get; set; }
        public string offset { get; set; }
        public int port { get; set; }
        public string token { get; set; }
        public int auth_port { get; set; }
        public string user { get; set; }
        public string pass { get; set; }
        public IRestClient client;
        public bool https { get; set; }
    }
}
