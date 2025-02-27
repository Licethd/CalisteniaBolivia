const initialState = {
    estado: "Not Found",
    data: false,
    usuario: {}
}

export default (state, action) => {
    if (!state) return initialState
    if (action.component == "caja") {
        switch (action.type) {
            case "registro":
                registro(state, action);
                break;
            case "cierre":
                cierre(state, action);
                break;
            case "getActiva":
                getActiva(state, action);
                break;
        }
        state.type = action.type;
        state.lastSend = new Date();
        state = { ...state };
    }
    return state;
}

const registro = (state, action) => {
    state.estado = action.estado
    if (action.estado === "exito") {
        state.usuario[action.data.key_usuario] = action.data;
        if (state.data) {
            state.data[action.data.key] = action.data;
        }
        state.lastRegister = action.data;
    }
}
const getActiva = (state, action) => {
    state.estado = action.estado
    if (action.estado === "exito") {
        state.usuario[action.data.key_usuario] = action.data;
        state.lastRegister = action.data;
    }
}
const cierre = (state, action) => {
    state.estado = action.estado
    if (action.estado === "exito") {
        delete state.usuario[action.data.key_usuario];
        // state.lastRegister = action.data;
    }
}
