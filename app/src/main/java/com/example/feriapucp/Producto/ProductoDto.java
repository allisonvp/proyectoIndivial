package com.example.feriapucp.Producto;

import com.example.feriapucp.Producto.Producto;

import java.io.Serializable;
import java.util.List;

public class ProductoDto implements Serializable {

    private List<Producto> listaProductos;

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }
}
