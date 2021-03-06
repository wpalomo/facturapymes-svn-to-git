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
import jm.fac.clas.Instalacion;
import jm.inf.clas.ServidorCorte;
import jm.web.Fecha;
import jm.web.Ftp;

/**
 *
 * @author Jorge
 */
public class setArchivoFtpServidorCorte extends HttpServlet {
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
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        HttpSession sesion = request.getSession(true);
        String usuario = (String)sesion.getAttribute("usuario");
        String clave = (String)sesion.getAttribute("clave");
        
        response.setContentType("text/plain;");
        response.setHeader("Content-disposition", "attachment; filename=colas_"+Fecha.getFecha("ISO")+"_"+Fecha.getHora()+".colas;");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "Mon, 01 Jan 2001 00:00:01 GMT");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Cache-Control", "must-revalidate");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter out = response.getWriter();
        
        //String idSuc = request.getParameter("idSuc");
        String msg = "Ha ocurrido un error inesperado por favor vuelva a intentarlo mas tarde";
        String tmp = System.getProperty("java.io.tmpdir");
        
        ServidorCorte objServidorCorte = new ServidorCorte(this._ip, this._puerto, this._db, usuario, clave);
        Instalacion objInstalacion = new Instalacion(this._ip, this._puerto, this._db, usuario, clave);
        
        try{
            ResultSet rsServidor = objServidorCorte.getServidoes();
            StringBuilder colas = new StringBuilder();
            StringBuilder listas = new StringBuilder();
            
            /*  COLAS   */
            while(rsServidor.next()){
                String id_servidor_ftp = rsServidor.getString("id_servidor_ftp")!=null ? rsServidor.getString("id_servidor_ftp") : "-1";
                String servidor = rsServidor.getString("servidor")!=null ? rsServidor.getString("servidor") : "";
                int puerto = rsServidor.getString("puerto")!=null ? rsServidor.getInt("puerto") : 26;
                String subredes = rsServidor.getString("subredes")!=null ? rsServidor.getString("subredes") : "";
                String usuario_ftp = rsServidor.getString("usuario")!=null ? rsServidor.getString("usuario") : "";
                String clave_ftp = rsServidor.getString("clave")!=null ? rsServidor.getString("clave") : "";
                String id_sucursal = rsServidor.getString("id_sucursal")!=null ? rsServidor.getString("id_sucursal") : "";
                
                String vecSubredes[] = subredes.split(",");
                String cliente = "";
                String ip = "";
                String burst_limit = "";
                String max_limit = "";
                String prioridad = "";
                String plan = "";
                long ancho_resi = 0;
                long ancho_small = 0;
                long ancho_noct = 0;
                long ancho_corp = 0;
                colas.append("/queue simple\n");
                colas.append("remove [find]\n");
                
                listas.append("/ip firewall address-list\n");
                listas.append("remove [find]\n");
                        
                try{
                    for(int i=0; i<vecSubredes.length; i++){
                        ResultSet res = objInstalacion.getColasServidor(vecSubredes[i], id_sucursal);
                        while(res.next()){
                            float ax_burst_limit = res.getString("burst_limit")!=null ? res.getFloat("burst_limit") : 0;
                            plan = res.getString("plan")!=null ? res.getString("plan") : "";
                            if(plan.toUpperCase().indexOf("RESIDENCIAL")>=0){
                                ancho_resi +=  ax_burst_limit;
                            }else if(plan.toUpperCase().indexOf("SMALL")>=0){
                                    ancho_small += ax_burst_limit;
                            }else if(plan.toUpperCase().indexOf("CORPORATIVO")>=0){
                                    ancho_corp += ax_burst_limit;
                            }else if(plan.toUpperCase().indexOf("NOCTURNO")>=0){
                                    ancho_noct += ax_burst_limit;
                            }
                        }

                        ancho_resi = ancho_resi/1024/20;
                        ancho_small = ancho_small/1024/4;
                        ancho_corp = ancho_corp/1024/2;
                        ancho_noct = ancho_noct/1024/20;

                        /*colas += "add max-limit="+ancho_resi+"M/"+ancho_resi+"M name=\"Residencial Total\" \\\n";
                        colas += "priority=2/2 total-priority=2\n";
                        colas += "add max-limit="+ancho_small+"M/"+ancho_small+"M name=\"Small Total\" \\\n";
                        colas += "priority=2/2 total-priority=2\n";
                        colas += "add max-limit="+ancho_noct+"M/"+ancho_noct+"M name=\"Nocturno Total\" \\\n";
                        colas += "priority=2/2 total-priority=2\n";
                        colas += "add max-limit="+ancho_corp+"M/"+ancho_corp+"M name=\"Corporativo Total\" \\\n";
                        colas += "priority=2/2 total-priority=2\n";*/

                        res.beforeFirst();
                        while(res.next()){
                            cliente = res.getString("razon_social")!=null ? res.getString("razon_social") : "";
                            ip = res.getString("ip")!=null ? res.getString("ip") : "";
                            burst_limit = res.getString("burst_limit")!=null ? res.getString("burst_limit") : "";
                            max_limit = res.getString("max_limit")!=null ? res.getString("max_limit") : "";
                            prioridad = res.getString("prioridad")!=null ? res.getString("prioridad") : "";
                            plan = res.getString("plan")!=null ? res.getString("plan") : "";

                            listas.append("add address="+ip+" comment=\""+cliente+"\" \\\nlist=activos\n");
                            if(plan.toUpperCase().indexOf("CORPORATIVO")>=0){
                                colas.append("add max-limit="+max_limit+"k/"+max_limit+"k name=\""+cliente+"\" \\\n");
                                colas.append("priority="+prioridad+"/"+prioridad+" target="+ip+"/32 total-priority="+prioridad+"\n");
                                listas.append("add address="+ip+" comment=\""+cliente+"\" \\\nlist=corporativos\n");
                            }else if(plan.toUpperCase().indexOf("RESIDENCIAL")>=0){
                                colas.append("add max-limit="+burst_limit+"k/"+burst_limit+"k name=\""+cliente+"\" \\\n");
                                colas.append("priority="+prioridad+"/"+prioridad+" target="+ip+"/32 total-priority="+prioridad+"\n");
                                listas.append("add address="+ip+" comment=\""+cliente+"\" \\\nlist=residenciales\n");
                            }else if(plan.toUpperCase().indexOf("SMALL")>=0){
                                colas.append("add max-limit="+burst_limit+"k/"+burst_limit+"k name=\""+cliente+"\" \\\n");
                                colas.append("priority="+prioridad+"/"+prioridad+" target="+ip+"/32 total-priority="+prioridad+"\n");
                                listas.append("add address="+ip+" comment=\""+cliente+"\" \\\nlist=small\n");
                            }else if(plan.toUpperCase().indexOf("NOCTURNO")>=0){
                                colas.append("add max-limit="+burst_limit+"k/"+burst_limit+"k name=\""+cliente+"\" \\\n");
                                colas.append("priority="+prioridad+"/"+prioridad+" target="+ip+"/32 total-priority="+prioridad+"\n");
                                listas.append("add address="+ip+" comment=\""+cliente+"\" \\\nlist=nocturnos\n");
                            }
                            
                            

                        }
                        res.close();
                    }    
                        
                }catch(Exception e){
                    msg = e.getMessage();
                }

                //  guardar datos en un archivo y enviar al servidor FTP
                this.grabarArchivo(tmp+"/colas.txt", colas);
                this.grabarArchivo(tmp+"/listas.txt", listas);

                objServidorCorte.actualizarMegas(id_servidor_ftp, ancho_corp, ancho_small, ancho_resi, ancho_noct);
                
                Ftp ftp = new Ftp();
                if(ftp.conectar(servidor, puerto, usuario_ftp, clave_ftp)){
                    ftp.subirArchivo(tmp+"/colas.txt", "/colas.txt");
                    ftp.subirArchivo(tmp+"/listas.txt", "/listas.txt");
                }
                ftp.desconectar();

                colas.delete(0, colas.length());  // limpio la variable de datos
                listas.delete(0, listas.length());  // limpio la variable de datos
            }
            
            

            msg = "Archivos subidos al FTP satisfactoriamente.";
            
        }catch(Exception e){
            msg = e.getMessage();
        }
        
        
        try {
            out.print("msg»" + msg);
        } finally {
            objServidorCorte.cerrar();
            objInstalacion.cerrar();
            out.close();
        }
    }

    
    public boolean grabarArchivo(String archivo, StringBuilder datos)
    {
        FileWriter fichero = null;
        PrintWriter pw = null;
        try{
            fichero = new FileWriter(archivo);
            pw = new PrintWriter(fichero);
            pw.print(datos);
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            try {
                if(null != fichero){
                    fichero.close();
                }
            }catch(Exception e2) {
                e2.printStackTrace();
            }
        }
        return true;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
