package com.example.feriapucp.Producto;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.feriapucp.Autenticacion.Usuario;
import com.example.feriapucp.R;
import com.example.feriapucp.Roles.ClienteActivity;
import com.google.android.gms.actions.ItemListIntents;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>{

    private List<Producto> listaproductos;
    private Context context;
    private String nombre;

    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public ProductoAdapter(List<Producto> listaproductos, Context context, String nombre) {
        this.listaproductos = listaproductos;
        this.context = context;
        this.nombre = nombre;
    }


    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview = LayoutInflater.from(context).inflate(R.layout.products_row_item, parent, false);
        ProductoViewHolder empleadoViewHolder = new ProductoViewHolder(itemview);
        return empleadoViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {

        Producto producto = listaproductos.get(position);
        StorageReference sto = storageReference.child(producto.getNombreFoto() + ".jpg");
        Glide.with(context).load(sto).into(holder.prodImg);

        holder.prodNom.setText(producto.getNombreProd());
        holder.prodMarca.setText(producto.getMarca());
        holder.prodPrecio.setText(producto.getPrecio());
        holder.prodCat.setText(producto.getCategoria());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(holder.itemView.getContext(), ProductoDetalle.class);
            intent.putExtra("producto", producto);
            holder.itemView.getContext().startActivity(intent);
            }
        });



    }

    @Override
    public int getItemCount() {
        return listaproductos.size();
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder{

        public ImageView prodImg;
        public TextView prodNom, prodMarca, prodPrecio, prodCat;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);

            this.prodImg = itemView.findViewById(R.id.prod_image);
            this.prodNom = itemView.findViewById(R.id.prod_name);
            this.prodMarca = itemView.findViewById(R.id.prod_marca);
            this.prodPrecio = itemView.findViewById(R.id.prod_price);
            this.prodCat = itemView.findViewById(R.id.prod_category);
        }
    }
}
