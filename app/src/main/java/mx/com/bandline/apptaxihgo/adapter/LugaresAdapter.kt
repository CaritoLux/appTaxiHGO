package mx.com.bandline.apptaxihgo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.model.dataLugar

// Un renglón por cada lugar que encontró Photon
class LugaresAdapter(
    private var lista: List<dataLugar>,
    private val alElegir: (dataLugar) -> Unit
) : RecyclerView.Adapter<LugaresAdapter.LugarViewHolder>() {

    class LugarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tv_nombre_lugar)
        val tvDetalle: TextView = itemView.findViewById(R.id.tv_detalle_lugar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LugarViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_lugar, parent, false)
        return LugarViewHolder(vista)
    }

    override fun onBindViewHolder(holder: LugarViewHolder, position: Int) {
        val lugar = lista[position]
        holder.tvNombre.text = lugar.nombre
        holder.tvDetalle.text = lugar.detalle
        holder.tvDetalle.visibility = if (lugar.detalle.isBlank()) View.GONE else View.VISIBLE
        holder.itemView.setOnClickListener { alElegir(lugar) }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nueva: List<dataLugar>) {
        lista = nueva
        notifyDataSetChanged()
    }
}
