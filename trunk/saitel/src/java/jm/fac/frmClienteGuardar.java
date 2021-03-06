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

package jm.fac;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import jm.fac.clas.Cliente;
import jm.seg.clas.Auditoria;

/**
 *
 * @author Jorge
 */
public class frmClienteGuardar extends HttpServlet {
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
        int id_sucursal = (Integer)sesion.getAttribute("sucursal");
        String usuario = (String)sesion.getAttribute("usuario");
        String clave = (String)sesion.getAttribute("clave");

        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "Mon, 01 Jan 2001 00:00:01 GMT");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Cache-Control", "must-revalidate");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter out = response.getWriter();
        
        Cliente objCliente = new Cliente(this._ip, this._puerto, this._db, usuario, clave);
        
        try {
            String r = "msg»El número establecimiento y cédula o del RUC ya existe.";
            String WHERE = request.getParameter("WHERE");
            String p = request.getParameter("p") != null ? request.getParameter("p") : "0";
            String id = request.getParameter("id");
            String tipo_documento = request.getParameter("tipo_documento");
            String establecimiento = request.getParameter("establecimiento");
            String ruc = request.getParameter("ru").toUpperCase();
            String razon_social = request.getParameter("rs").toUpperCase();
            String fecha_nacimiento = request.getParameter("fecha_nacimiento");
            String id_plan_cuenta = request.getParameter("idPC");
            //String id_lista_precio = request.getParameter("liPr");
            String direccion = request.getParameter("di").toUpperCase();
            String id_provincia = request.getParameter("prv");
            String id_ciudad = request.getParameter("ci");
            //String id_parroquia = request.getParameter("prr");
            String pais = request.getParameter("pa").toUpperCase();
            String carne_conadis = request.getParameter("carne_conadis");
            String telefono = request.getParameter("te");
            String movil_claro = request.getParameter("te_cl");
            String movil_movistar = request.getParameter("te_mo");
            
            String fax = request.getParameter("fa");
            String correo = request.getParameter("ma");
            String web = request.getParameter("we");
            String contacto = request.getParameter("co");
            String observacion = request.getParameter("ob");
            
            String tipo_doc_debito = request.getParameter("tipo_doc_debito");
            String documento = request.getParameter("documento");
            String cliente_debito = request.getParameter("cliente_debito").toUpperCase();
            String forma_pago = request.getParameter("forma_pago");
            String tipo_cuenta = request.getParameter("tipo_cuenta");
            String tipo_tarjeta_credito = request.getParameter("tipo_tarjeta_credito");
            String tarjeta_credito_caduca = request.getParameter("tarjeta_credito_caduca");
            String num_cuenta = request.getParameter("num_cuenta");

            if(!objCliente.estaDuplicado(id, establecimiento, ruc)){
                r = "msg»Ha ocurrido un error inesperado, por favor, vuelva a intentarlo más tarde o " +
                    "contáctese con el administrador del sistema para mayor información.";
                if(id.compareTo("-1")==0){
                    String idC = objCliente.insertar(id_sucursal, establecimiento, tipo_documento, ruc, razon_social, fecha_nacimiento,
                            id_plan_cuenta, direccion, pais, id_provincia, id_ciudad, carne_conadis, telefono, 
                            movil_claro, movil_movistar, fax, correo, web, contacto, observacion,
                            tipo_doc_debito, documento, cliente_debito, forma_pago, tipo_cuenta, tipo_tarjeta_credito, 
                            tarjeta_credito_caduca, num_cuenta);
                    if(idC.compareTo("-1")!=0){
                        Auditoria auditoria = new Auditoria(this._ip, this._puerto, this._db, usuario, clave);
                        auditoria.setRegistro(request, "INGRESO DEL NUEVO CLIENTE: "+razon_social);
                        auditoria.cerrar();
                        r = "err»0^vta»cmp^tbl»"+objCliente.paginar("vta_cliente", "id_cliente,ruc,razon_social", WHERE, Integer.parseInt(p), 100)+
                                "^fun»fac_clienteEditar("+idC+");^msg»Información ingresada satisfactoriamente.";
                    }
                }else{
                    if(objCliente.actualizar(id, establecimiento, tipo_documento, ruc, razon_social, fecha_nacimiento, 
                            id_plan_cuenta, direccion, pais, id_provincia, id_ciudad,
                            carne_conadis, telefono, movil_claro, movil_movistar, fax, correo, web, contacto, observacion,
                            tipo_doc_debito, documento, cliente_debito, forma_pago, tipo_cuenta, tipo_tarjeta_credito, 
                            tarjeta_credito_caduca, num_cuenta)){
                        Auditoria auditoria = new Auditoria(this._ip, this._puerto, this._db, usuario, clave);
                        auditoria.setRegistro(request, "ACTUALIZACION DE LA INFORMACION DEL CLIENTE: "+razon_social);
                        auditoria.cerrar();
                        r = "err»0^vta»cmp^tbl»"+objCliente.paginar("vta_cliente", "id_cliente,ruc,razon_social", WHERE, Integer.parseInt(p), 100)+"^msg»Información guardada satisfactoriamente.";
                    }
                }
            }
            
            out.print(r);
        } finally {
            objCliente.cerrar();
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