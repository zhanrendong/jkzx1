python3 init_auth.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
python3 init_instruments.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
python3 init_market_data.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
python3 init_calendars.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
python3 init_models.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
python3 init_pe.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
python3 init_client.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
python3 init_report_test_setup.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
python3 init_trades.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
python3 init_lcm.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
python3 init_report.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
python3 init_doc_template.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
python3 init_poi_template.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
python3 test_document.py
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi