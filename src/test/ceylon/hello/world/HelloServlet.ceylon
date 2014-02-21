import javax.servlet.http { HttpServlet, HttpServletRequest, HttpServletResponse }
import ceylon.html { ... }

shared class HelloServlet() extends HttpServlet() {
    
    shared actual void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.contentType = "text/html";
        
        Html page = Html {
            html5;
            Head {
                title = "Hello World";
            };
            Body {
                Div("page content")
            };
        };
        
        response.writer.print(page);
    }
}