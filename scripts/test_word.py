import win32com.client
for prog in ["Word.Application", "Kwps.Application", "wps.Application", "WPS.Application"]:
    try:
        w = win32com.client.Dispatch(prog)
        print("OK", prog, getattr(w, "Name", "?"))
        w.Quit()
    except Exception as e:
        print("FAIL", prog, e)