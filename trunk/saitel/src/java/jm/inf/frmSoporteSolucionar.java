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

import javax.servlet.*;
import javax.servlet.http.*;
import jm.adm.clas.Configuracion;
import jm.adm.clas.Mensaje;
import jm.fac.clas.Instalacion;
import jm.inf.clas.OrdenTrabajo;
import jm.inf.clas.Soporte;
import jm.seg.clas.Auditoria;
import jm.web.Correo;
import jm.web.Fecha;

/**
 *
 * @author Jorge
 */
public class frmSoporteSolucionar extends HttpServlet {
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
        String empleado = (String)sesion.getAttribute("empleado");
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
        
        Soporte objSoporte = new Soporte(this._ip, this._puerto, this._db, usuario, clave);
        
        try {
            String r = "msg»Ha ocurrido un error inesperado, por favor, vuelva a intentarlo más tarde o " +
                    "contáctese con el administrador del sistema para mayor información.";
            //String WHERE = request.getParameter("WHERE");
            String id = request.getParameter("id");
            String id_instalacion = request.getParameter("idIns1");
            String num_soporte = request.getParameter("num_soporte1");
            String problema = request.getParameter("problema1");
            String recomendacion = request.getParameter("recomendacion");
            String genOrdenTrabajo = request.getParameter("odTra");
            String antena_acoplada = request.getParameter("antena_acoplada");

            
            if(objSoporte.solucionar(id, id_instalacion, usuario, recomendacion)){
                Auditoria auditoria = new Auditoria(this._ip, this._puerto, this._db, usuario, clave);
                auditoria.setRegistro(request, "REGISTRO DE SOLUCION DE SOPORTE NRO. "+ id_sucursal_sesion + "-" +num_soporte);

                Instalacion objInstalacion = new Instalacion(this._ip, this._puerto, this._db, usuario, clave);
                String email_cliente = objInstalacion.getMailInstalacion(id_instalacion);
                objInstalacion.setAntenaAcoplada(id_instalacion, antena_acoplada);
                objInstalacion.cerrar();

                String msg = "La solución ha sido guardada satisfactoriamente. "
                        + "Pero no ha sido posible el envío del e-mail al cliente debido a que no posee una dirección de correo electrónico.";
                if(email_cliente.compareTo("")!=0){
                    Configuracion conf = new Configuracion(this._ip, this._puerto, this._db, usuario, clave);
                    String mail_svr = conf.getValor("mail_svr");
                    String mail_origen = conf.getValor("mail_origen");
                    conf.cerrar();

                    String mensaje = "Estimado cliente\n\n";
                    mensaje += "Hemos solucionado su novedad Nro. "+ id_sucursal_sesion + "-" +num_soporte + " ("+objSoporte.decodificarURI(problema)+").\n\n";
                    mensaje += "Nuestras sugerencias y/o recomendaciones para dicho inconveniente son:\n"+objSoporte.decodificarURI(recomendacion)+"\n\n\n";
                    mensaje += "Saludos cordiales\n\n";
                    mensaje += "DEPARTAMENTO DE SERVICIO AL CLIENTE";

                    msg = "La solución ha sido guardada satisfactoriamente. Pero ha ocurrido un error al tratar de enviar el e-mail,"
                            + " Por favor, contáctese con el administrador del sistema para mayor información.";
                    if(Correo.enviar(mail_svr, mail_origen, email_cliente, "", "", "AVISO DE SOLUCION", new StringBuilder(mensaje), false)){
                        msg = "Solución guardada satisfactoriamente.";
                    }
                }

                r = "err»0^vta»cmp^msg»"+msg+"^fun»fac_tblSoporte();";
                
                if(genOrdenTrabajo.compareTo("true")==0){
                    String tipo_trabajo = request.getParameter("tipo_trabajo");
                    String fecha_cliente = request.getParameter("fecha_cliente");
                    String hora_cliente = request.getParameter("hora_cliente");
                    String diagnostico_tecnico = request.getParameter("diagnostico_tecnico");

                    OrdenTrabajo objOrdenTrabajo = new OrdenTrabajo(this._ip, this._puerto, this._db, usuario, clave);
                    String num_orden = objOrdenTrabajo.getNumOrden(id_sucursal_sesion);
                    String id_orden_trabajo = objOrdenTrabajo.insertar(id_instalacion, id_sucursal_sesion, num_orden, tipo_trabajo, usuario, fecha_cliente, hora_cliente, diagnostico_tecnico);
                    if(id_orden_trabajo.compareTo("-1")!=0){
                        auditoria.setRegistro(request, "INGRESO DE UNA NUEVA ORDEN DE TRABAJO NRO. "+ id_sucursal_sesion + "-" +num_orden);

                        Mensaje objMensaje = new Mensaje(this._ip, this._puerto, this._db, usuario, clave);
                        objMensaje.setMensaje(usuario, empleado + " ha generado una nueva orden de trabajo No. " + num_orden + " desde el módulo de soportes", "trabajo", id_orden_trabajo, Fecha.getFecha("ISO"));
                        objMensaje.cerrar();

                    //r += "imprimir('pdfOrdenTrabajo?id="+idOrdTra+"');";
                    }
                    objOrdenTrabajo.cerrar();
                }
                auditoria.cerrar();
            }

            out.print(r);
        } finally {
            objSoporte.cerrar();
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