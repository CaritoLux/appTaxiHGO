package mx.com.bandline.apptaxihgo.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.com.bandline.apptaxihgo.R
import mx.com.bandline.apptaxihgo.model.dataViajeResumen
import mx.com.bandline.apptaxihgo.view.ActivityMisViajes
import mx.com.bandline.apptaxihgo.view.clsFormatoViaje

class ViajeAdapter(
    private val activity: ActivityMisViajes,
    private var listaViajes: MutableList<dataViajeResumen>
) : RecyclerView.Adapter<ViajeAdapter.ViajeViewHolder>() {

    class ViajeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDestino: TextView = itemView.findViewById(R.id.tv_destino)
        val tvOrigen: TextView = itemView.findViewById(R.id.tv_origen)
        val tvFecha: TextView = itemView.findViewById(R.id.tv_fecha)
        val tvTarifa: TextView = itemView.findViewById(R.id.tv_tarifa)
        val tvEstado: TextView = itemView.findViewById(R.id.tv_estado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViajeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_viaje, parent, false)
        return ViajeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViajeViewHolder, position: Int) {
        val viaje = listaViajes[position]
        holder.tvDestino.text = viaje.destino.substringBefore(",")
        holder.tvOrigen.text = "Desde " + viaje.origen.substringBefore(",")
        holder.tvFecha.text = clsFormatoViaje.fechaLista(viaje.fecha)
        holder.tvTarifa.text = clsFormatoViaje.moneda(viaje.tarifa)
        clsFormatoViaje.pintarEstado(holder.tvEstado, viaje.estado, viaje.estadoTexto)

        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // Se ejecuta en ActivityMisViajes, que abre el detalle
                activity.seleccionarViaje(viaje)
            }
        })
    }

    override fun getItemCount(): Int {
        return listaViajes.size
    }

    fun actualizarLista(nuevaLista: List<dataViajeResumen>) {
        listaViajes = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    fun agregarViajes(mas: List<dataViajeResumen>) {
        val inicio = listaViajes.size
        listaViajes.addAll(mas)
        notifyItemRangeInserted(inicio, mas.size)
    }
}
