/**
* @version 1.0
* @package FACTURAPYMES.
* @author Jorge Washington Mueses Cevallos.
* @copyright Copyright (C) 2010 por Jorge Mueses. Todos los derechos reservados.
* @license http://www.gnu.org/copyleft/gpl.html GNU/GPL.
* FACTURAPYMES! es un software de libre distribución, que puede ser
* copiado y distribuido bajo los términos de la Licencia Pública
* General GNU, de acuerdo con la publicada por la Free Software
* Foundation, versión 2 de la licencia o cualquier versión posterior.
*/

package jm.inf;

import java.io.*;
import java.sql.ResultSet;

import javax.servlet.*;
import javax.servlet.http.*;
import jm.inv.clas.Producto;

/**
 *
 * @author Jorge
 */
public class getProductosMateriales extends HttpServlet {
    private String _ip = null;
    private int _puerto = 5432;
    private String _db = null;

    public void init(ServletConfig config) throws ServletException
    {
        this._ip = config.getServletContext().getInitParameter("_IP");
        this._puerto = Integer.parseInt(config.getServletContext().getInitParameter("_PUERTO"));
        this._db = config.getServletContext().getInitParameter("_DB");
    }
    /**
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        HttpSession sesion = request.getSession(true);
        int id_sucursal_sesion = (Integer)sesion.getAttribute("sucursal");
        String usuario = (String)sesion.getAttribute("usuario");
        String clave = (String)sesion.getAttribute("clave");

        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "Mon, 01 Jan 2001 00:00:01 GMT");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Cache-Control", "must-revalidate");
        response.setHeader("Cache-Control", "no-cache");

        PrintWriter out = response.getWriter();

        String texto = request.getParameter("t");
        //String objetoContenedor = request.getParameter("d");
        //String objeto = request.getParameter("o");
        String funcion = request.getParameter("f");
        Producto objProducto = new Producto(this._ip, this._puerto, this._db, usuario, clave);
        try {
            String combo="";
            try{
                ResultSet rs = objProducto.getProductosVenta(id_sucursal_sesion, texto);
                int i=0;
                while(rs.next()){
                    combo+="<div onclick=\""+funcion + "(" + i + ")\" onmouseover=\"this.className='jm_filaImp'\" onmouseout=\"this.className=''\">"
                            + "<input type='hidden' id='idPr"+i+"' value='"+(rs.getString("id_producto")!=null ? rs.getString("id_producto") : "")+"' />"+
                    "<span id='desPr"+i+"'>"+(rs.getString("descripcion")!=null ? rs.getString("descripcion") : "")+"</span></div>";
                    i++;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            out.print("obj»Fech_^frm»"+combo);
        } finally {
            objProducto.cerrar();
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
