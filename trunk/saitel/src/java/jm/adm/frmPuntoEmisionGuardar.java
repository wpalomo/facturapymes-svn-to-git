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

package jm.adm;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import jm.adm.clas.PuntoEmision;
import jm.seg.clas.Auditoria;

/**
 *
 * @author Jorge
 */
public class frmPuntoEmisionGuardar extends HttpServlet {
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
        String usuario = (String)sesion.getAttribute("usuario");
        String clave = (String)sesion.getAttribute("clave");
        
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "Mon, 01 Jan 2001 00:00:01 GMT");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Cache-Control", "must-revalidate");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter out = response.getWriter();
        
        PuntoEmision objPuntoEmision = new PuntoEmision(this._ip, this._puerto, this._db, usuario, clave);
        try {
            String r = "msg»El nombre de la PuntoEmision ya existe o el usuario ya se encuentra en otra PuntoEmision.";
            //String WHERE = request.getParameter("WHERE");
            String id = request.getParameter("id_punto_emision");
            String id_sucursal = request.getParameter("idSucursal");
            String punto_emision = request.getParameter("punto_emision").toUpperCase();
            String usuario_caja = request.getParameter("usuario_caja");
            String id_plan_cuenta_caja = request.getParameter("id_plan_cuenta_caja");
            String id_plan_cuenta_diferencia_caja = request.getParameter("id_plan_cuenta_diferencia_caja");
            String fac_num_serie = request.getParameter("fac_num_serie");
            String fac_sec_desde = request.getParameter("fac_sec_desde");
            String fac_sec_hasta = request.getParameter("fac_sec_hasta");
            String fac_cad_facturero = request.getParameter("fac_cad_facturero");
            String fac_autorizacion = request.getParameter("fac_autorizacion");
            String num_fact_inicial = request.getParameter("num_fact_inicial");
            //String estado = request.getParameter("estado");

            if(!objPuntoEmision.estaDuplicado(id, punto_emision) && !objPuntoEmision.ipDuplicada(id, usuario_caja)){
                r = "msg»Ha ocurrido un error inesperado, por favor, vuelva a intentarlo más tarde o " +
                        "contáctese con el administrador del sistema para mayor información.";
                if(id.compareTo("-1")==0){
                    String pk = objPuntoEmision.insertar(id_sucursal, punto_emision,usuario_caja,id_plan_cuenta_caja,id_plan_cuenta_diferencia_caja,fac_num_serie,fac_sec_desde,fac_sec_hasta,fac_cad_facturero,
                            fac_autorizacion, num_fact_inicial);
                    if(pk.compareTo("-1")!=0){
                        Auditoria auditoria = new Auditoria(this._ip, this._puerto, this._db, usuario, clave);
                        auditoria.setRegistro(request, "INGRESO DE LA NUEVO PUNTO DE EMISION : "+punto_emision);
                        auditoria.cerrar();
                        r = "obj»axTblPE^frm»"+objPuntoEmision.tblPuntosEmision(id_sucursal)+
                            "^msg»Información ingresada satisfactoriamente.";
                    }
                }else{
                    if(objPuntoEmision.actualizar(id, punto_emision,usuario_caja,id_plan_cuenta_caja,id_plan_cuenta_diferencia_caja,fac_num_serie,fac_sec_desde,fac_sec_hasta,fac_cad_facturero,
                            fac_autorizacion, num_fact_inicial)){
                        Auditoria auditoria = new Auditoria(this._ip, this._puerto, this._db, usuario, clave);
                        auditoria.setRegistro(request, "ACTUALIZACION DE LA INFORMACION DE LA PuntoEmision: "+punto_emision);
                        auditoria.cerrar();

                        r = "obj»axTblPE^frm»"+objPuntoEmision.tblPuntosEmision(id_sucursal)+
                            "^msg»Información guardada satisfactoriamente.";
                    }
                }
            }
            out.print(r);
            
        } finally {
            objPuntoEmision.cerrar();
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
