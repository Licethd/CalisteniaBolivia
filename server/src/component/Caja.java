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
import Server.SSSAbstract.SSServerAbstract;
import Server.SSSAbstract.SSSessionAbstract;

public class Caja {

    public Caja(JSONObject data, SSSessionAbstract session) {
        switch (data.getString("type")) {
            case "getAll":
                getAll(data, session);
            break;
            case "getByKey":
                getByKey(data, session);
                break;
            case "getActiva":
                getActiva(data, session);
                break;
            case "registro":
                registro(data, session);
            break;
            case "cierre":
                cierre(data, session);
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
            String consulta =  "select get_all('caja') as json";
            JSONObject data = Conexion.ejecutarConsultaObject(consulta);
            Conexion.historico(obj.getString("key_usuario"), "caja_getAll", data);
            obj.put("data", data);
            obj.put("estado", "exito");
        } catch (SQLException e) {
            obj.put("estado", "error");
            e.printStackTrace();
        }
    }
    
    public void getActiva(JSONObject obj, SSSessionAbstract session) {
        try {
            String consulta =  "select caja_get_activa('"+obj.getString("key_usuario")+"') as json";
            JSONObject data = Conexion.ejecutarConsultaObject(consulta);
            Conexion.historico(obj.getString("key_usuario"), "caja_get_activa", data);

            obj.put("data", data);
            obj.put("estado", "exito");
        } catch (SQLException e) {
            obj.put("estado", "error");
            e.printStackTrace();
        }
    }

    public static JSONObject getActiva(String key_usuario) {
        try {
            String consulta =  "select caja_get_activa('"+key_usuario+"') as json";
            return Conexion.ejecutarConsultaObject(consulta);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getByKey(JSONObject obj, SSSessionAbstract session) {
        try {
            String consulta =  "select get_by_key('caja','"+obj.getString("key")+"') as json";
            JSONObject data = Conexion.ejecutarConsultaObject(consulta);
            Conexion.historico(obj.getString("key_usuario"), "caja_getByKey", data);

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
            JSONObject caja = obj.getJSONObject("data");
            caja.put("key",UUID.randomUUID().toString());
            caja.put("fecha_on",fecha_on);
            caja.put("estado",1);
            Conexion.insertArray("caja", new JSONArray().put(caja));

            JSONObject caja_movimiento = new JSONObject();
            caja_movimiento.put("key", UUID.randomUUID().toString());
            caja_movimiento.put("key_caja", caja.getString("key"));
            caja_movimiento.put("key_caja_tipo_movimiento", 1);
            caja_movimiento.put("descripcion", "apertura");
            caja_movimiento.put("monto", caja.getDouble("monto"));
            caja_movimiento.put("data", "");
            caja_movimiento.put("fecha_on", fecha_on);
            caja_movimiento.put("estado", 1);
            Conexion.insertArray("caja_movimiento", new JSONArray().put(caja_movimiento));

            Conexion.historico(obj.getString("key_usuario"), caja.getString("key"), "caja_registro", caja);
            obj.put("data", caja);
            obj.put("estado", "exito");

            //SSServerAbstract.sendServer(SSServerAbstract.TIPO_SOCKET_WEB, obj.toString());
            SSServerAbstract.sendServer(SSServerAbstract.TIPO_SOCKET, obj.toString());
        } catch (SQLException e) {
            obj.put("estado", "error");
            e.printStackTrace();
        }
    }


    public static JSONObject addVentaServicio(String key_caja, String key_usuario, String key_tipo_pago, double monto, JSONObject data) throws SQLException{
        JSONObject caja_movimiento = new JSONObject();
        caja_movimiento.put("key", UUID.randomUUID().toString());
        caja_movimiento.put("key_caja", key_caja);
        caja_movimiento.put("key_caja_tipo_movimiento", 3);
        caja_movimiento.put("key_tipo_pago", key_tipo_pago);
        caja_movimiento.put("descripcion", "Venta de servicio");
        caja_movimiento.put("monto", monto);
        caja_movimiento.put("data", data);
        caja_movimiento.put("fecha_on", "now()");
        caja_movimiento.put("estado", 1);
        Conexion.insertArray("caja_movimiento", new JSONArray().put(caja_movimiento));

        return caja_movimiento;
    }

    public void cierre(JSONObject obj, SSSessionAbstract session) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            String fecha_on = formatter.format(new Date());
            JSONObject caja = obj.getJSONObject("data");
            caja.put("fecha_off", fecha_on);
            Conexion.editObject("caja", caja);

            JSONObject caja_movimiento = new JSONObject();
            caja_movimiento.put("key", UUID.randomUUID().toString());
            caja_movimiento.put("key_caja", caja.getString("key"));
            caja_movimiento.put("key_caja_tipo_movimiento", 2);
            caja_movimiento.put("descripcion", "cierre");
            caja_movimiento.put("monto", caja.getDouble("monto"));
            caja_movimiento.put("data", "");
            caja_movimiento.put("fecha_on", fecha_on);
            caja_movimiento.put("estado", 1);
            Conexion.insertArray("caja_movimiento", new JSONArray().put(caja_movimiento));

            Conexion.historico(obj.getString("key_usuario"), caja.getString("key"), "caja_registro", caja);
            obj.put("data", caja);
            obj.put("estado", "exito");

            //SSServerAbstract.sendServer(SSServerAbstract.TIPO_SOCKET_WEB, obj.toString());
            SSServerAbstract.sendServer(SSServerAbstract.TIPO_SOCKET, obj.toString());
        } catch (SQLException e) {
            obj.put("estado", "error");
            e.printStackTrace();
        }
    }

    public void editar(JSONObject obj, SSSessionAbstract session) {
        try {
            JSONObject caja = obj.getJSONObject("data");
            Conexion.editObject("caja", caja);
            Conexion.historico(obj.getString("key_usuario"), caja.getString("key"), "caja_editar", caja);
            obj.put("data", caja);
            obj.put("estado", "exito");
            //SSServerAbstract.sendServer(SSServerAbstract.TIPO_SOCKET_WEB, obj.toString());
            SSServerAbstract.sendServer(SSServerAbstract.TIPO_SOCKET, obj.toString());
        } catch (SQLException e) {
            obj.put("estado", "error");
            obj.put("error", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void subirFoto(JSONObject obj, SSSessionAbstract session) {
        String url = Config.getJSON().getJSONObject("files").getString("url");
        File f = new File(url+"caja/");
        Conexion.historico(obj.getString("key_usuario"), obj.getString("key"), "caja_subirFoto", new JSONObject());
        if(!f.exists()) f.mkdirs();
        obj.put("dirs", new JSONArray().put(f.getPath()+"/"+obj.getString("key")));
        obj.put("estado", "exito");
        //SSServerAbstract.sendServer(SSServerAbstract.TIPO_SOCKET_WEB, obj.toString());
        SSServerAbstract.sendServer(SSServerAbstract.TIPO_SOCKET, obj.toString());
    }
}