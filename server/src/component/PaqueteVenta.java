package component;

import java.io.File;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import conexion.*;
import SocketCliente.SocketCliete;
import org.json.JSONArray;
import org.json.JSONObject;

import Config.Config;
import Email.Email;
import Server.SSSAbstract.SSServerAbstract;
import Server.SSSAbstract.SSSessionAbstract;

public class PaqueteVenta {

    public PaqueteVenta(JSONObject data, SSSessionAbstract session) {
        switch (data.getString("type")) {
            case "getAll":
                getAll(data, session);
            break;
            case "getAllByUsuario":
                getAllByUsuario(data, session);
            break;
            case "getByKey":
                getByKey(data, session);
                break;
            case "registro":
                registro(data, session);
            break;
            case "editar":
                editar(data, session);
            break;
            case "subirFoto":
                subirFoto(data, session);
            break;
            default:
                defaultType(data, session);
        }
    }

    public void defaultType(JSONObject obj, SSSessionAbstract session) {
        SocketCliete.send("usuario", obj, session);
    }

    public void getAll(JSONObject obj, SSSessionAbstract session) {
        try {
            String consulta =  "select get_all('paquete_venta','key_paquete','"+obj.getString("key_paquete")+"') as json";
            JSONObject data = Conexion.ejecutarConsultaObject(consulta);
            obj.put("data", data);
            obj.put("estado", "exito");
        } catch (SQLException e) {
            obj.put("estado", "error");
            e.printStackTrace();
        }
    }

    public void getAllByUsuario(JSONObject obj, SSSessionAbstract session) {
        try {
            String consulta =  "select paquete_venta_get_all('"+obj.getString("key_usuario")+"') as json";
            JSONObject data = Conexion.ejecutarConsultaObject(consulta);
            obj.put("data", data);
            obj.put("estado", "exito");
        } catch (SQLException e) {
            obj.put("estado", "error");
            e.printStackTrace();
        }
    }

    public void getByKey(JSONObject obj, SSSessionAbstract session) {
        try {
            String consulta =  "select get_by_key('paquete_venta','"+obj.getString("key")+"') as json";
            JSONObject data = Conexion.ejecutarConsultaObject(consulta);
            Conexion.historico(obj.getString("key_usuario"), "paquete_venta_getByKey", data);

            obj.put("data", data);
            obj.put("estado", "exito");
        } catch (SQLException e) {
            obj.put("estado", "error");
            e.printStackTrace();
        }
    }

    public void registro(JSONObject obj, SSSessionAbstract session) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            String fecha_on = formatter.format(new Date());
            JSONObject paquete_venta = obj.getJSONObject("data");
            paquete_venta.put("key",UUID.randomUUID().toString());
            paquete_venta.put("fecha_on",fecha_on);
            paquete_venta.put("estado",1);

            JSONObject caja_activa = Caja.getActiva(obj.getString("key_usuario"));
            if(caja_activa == null){
                obj.put("estado", "error");
                obj.put("error", "sin_caja");
                return;
            }

            JSONArray clientes = obj.getJSONArray("clientes");
            JSONArray paquete_venta_usuarios = new JSONArray();
            JSONObject paquete_venta_usuario;
            for (int i = 0; i < clientes.length(); i++) {
                paquete_venta_usuario = new JSONObject();
                paquete_venta_usuario.put("key", UUID.randomUUID().toString());
                paquete_venta_usuario.put("key_usuario",clientes.getJSONObject(i).getString("key"));
                paquete_venta_usuario.put("fecha_inicio",clientes.getJSONObject(i).getString("fecha_inicio"));
                paquete_venta_usuario.put("fecha_fin",clientes.getJSONObject(i).getString("fecha_fin"));
                paquete_venta_usuario.put("key_paquete_venta",paquete_venta.get("key"));
                paquete_venta_usuario.put("fecha_on",fecha_on);
                paquete_venta_usuario.put("estado",1);
                paquete_venta_usuarios.put(paquete_venta_usuario);
            }
            String key_tipo_pago = null;
            if(paquete_venta.has("key_tipo_pago")){
                if(!paquete_venta.isNull("key_tipo_pago")){
                    key_tipo_pago = paquete_venta.getString("key_tipo_pago");
                }
            }
            JSONObject caja_movimiento = Caja.addVentaServicio(caja_activa.getString("key"), obj.getString("key_usuario"), key_tipo_pago, paquete_venta.getDouble("monto"), paquete_venta.getJSONObject("data"));
            paquete_venta.put("key_caja_movimiento", caja_movimiento.getString("key"));

            Conexion.insertArray("paquete_venta", new JSONArray().put(paquete_venta));
            Conexion.insertArray("paquete_venta_usuario", paquete_venta_usuarios);
            Conexion.historico(obj.getString("key_usuario"), paquete_venta.getString("key"), "paquete_venta_registro", paquete_venta);

            JSONArray clients = new JSONArray(clientes.toString()); 

            obj.put("data", paquete_venta); 
            obj.put("clientes", paquete_venta_usuarios); 
            obj.put("estado", "exito");


            SSServerAbstract.sendUsers(obj.toString(), new JSONArray().put(obj.getString("key_usuario")));
            

            JSONObject mail = new JSONObject();
            
            
            mail.put("__ID_PEDIDO__",paquete_venta.getString("key"));
            mail.put("__PAQUETE__",paquete_venta.getString("nombre_paquete"));
            mail.put("__MONTO__",paquete_venta.get("monto").toString());
            mail.put("__KEY_PAQUETE__",paquete_venta.getString("key_paquete")+"?fecha="+new Date().toString());
            
            for(int i = 0; i<clients.length(); i++){
                JSONObject cliente = clients.getJSONObject(i);
                mail.put("__FECHA__",cliente.getString("fecha_inicio"));
                mail.put("__RENOVACION__",cliente.getString("fecha_fin"));
                mail.put("__MAIL__",cliente.getString("Correo"));
                mail.put("__KEY_USUARIO_CLIENTE__",paquete_venta_usuarios.getJSONObject(i).getString("key")+"?fecha="+new Date().toString());
                mail.put("__CI__",cliente.getString("CI"));
                new Email(Email.TIPO_RECIBO, mail);
            }
            

        } catch (SQLException e) {
            obj.put("estado", "error");
            e.printStackTrace();
        }

    }

    public void editar(JSONObject obj, SSSessionAbstract session) {
        try {
            JSONObject paquete_venta = obj.getJSONObject("data");
            Conexion.editObject("paquete_venta", paquete_venta);
            Conexion.historico(obj.getString("key_usuario"), paquete_venta.getString("key"), "paquete_venta_editar", paquete_venta);
            obj.put("data", paquete_venta);
            obj.put("estado", "exito");
            SSServerAbstract.sendServer(SSServerAbstract.TIPO_SOCKET_WEB, obj.toString());
            SSServerAbstract.sendServer(SSServerAbstract.TIPO_SOCKET, obj.toString());
        } catch (SQLException e) {
            obj.put("estado", "error");
            obj.put("error", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void subirFoto(JSONObject obj, SSSessionAbstract session) {
        String url = Config.getJSON().getJSONObject("files").getString("url");
        File f = new File(url+"paquete_venta/");
        Conexion.historico(obj.getString("key_usuario"), obj.getString("key"), "paquete_venta_subirFoto", new JSONObject());
        if(!f.exists()) f.mkdirs();
        obj.put("dirs", new JSONArray().put(f.getPath()+"/"+obj.getString("key")));
        obj.put("estado", "exito");
        SSServerAbstract.sendServer(SSServerAbstract.TIPO_SOCKET_WEB, obj.toString());
        SSServerAbstract.sendServer(SSServerAbstract.TIPO_SOCKET, obj.toString());
    }
}