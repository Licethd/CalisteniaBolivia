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

public class CajaMovimiento {

    public CajaMovimiento(JSONObject data, SSSessionAbstract session) {
        switch (data.getString("type")) {
            case "getAll":
                getAll(data, session);
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
            String consulta =  "select get_all('caja_movimiento', 'key_caja', '"+obj.getString("key_caja")+"') as json";
            JSONObject data = Conexion.ejecutarConsultaObject(consulta);
            Conexion.historico(obj.getString("key_usuario"), "caja_movimiento_getAll", data);
            obj.put("data", data);
            obj.put("estado", "exito");
        } catch (SQLException e) {
            obj.put("estado", "error");
            e.printStackTrace();
        }
    }

    public void getByKey(JSONObject obj, SSSessionAbstract session) {
        try {
            String consulta =  "select get_by_key('caja_movimiento','"+obj.getString("key")+"') as json";
            JSONObject data = Conexion.ejecutarConsultaObject(consulta);
            Conexion.historico(obj.getString("key_usuario"), "caja_movimiento_getByKey", data);

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
           
            JSONObject caja_movimiento = new JSONObject();
            caja_movimiento.put("key", UUID.randomUUID().toString());
            caja_movimiento.put("key_caja_movimiento", caja_movimiento.getString("key"));
            caja_movimiento.put("key_caja_movimiento_tipo_movimiento", 1);
            caja_movimiento.put("descripcion", "apertura");
            caja_movimiento.put("monto", caja_movimiento.getDouble("monto"));
            caja_movimiento.put("data", "");
            caja_movimiento.put("fecha_on", fecha_on);
            caja_movimiento.put("estado", 1);
            Conexion.insertArray("caja_movimiento", new JSONArray().put(caja_movimiento));

            Conexion.historico(obj.getString("key_usuario"), caja_movimiento.getString("key"), "caja_movimiento_registro", caja_movimiento);
            obj.put("data", caja_movimiento);
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
            JSONObject caja_movimiento = obj.getJSONObject("data");
            Conexion.editObject("caja_movimiento", caja_movimiento);
            Conexion.historico(obj.getString("key_usuario"), caja_movimiento.getString("key"), "caja_movimiento_editar", caja_movimiento);
            obj.put("data", caja_movimiento);
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
        File f = new File(url+"caja_movimiento/");
        Conexion.historico(obj.getString("key_usuario"), obj.getString("key"), "caja_movimiento_subirFoto", new JSONObject());
        if(!f.exists()) f.mkdirs();
        obj.put("dirs", new JSONArray().put(f.getPath()+"/"+obj.getString("key")));
        obj.put("estado", "exito");
        //SSServerAbstract.sendServer(SSServerAbstract.TIPO_SOCKET_WEB, obj.toString());
        SSServerAbstract.sendServer(SSServerAbstract.TIPO_SOCKET, obj.toString());
    }
}