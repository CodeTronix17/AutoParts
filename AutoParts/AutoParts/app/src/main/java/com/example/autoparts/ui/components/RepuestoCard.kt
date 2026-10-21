package com.example.autoparts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.autoparts.data.remote.ApiConfig
import com.example.autoparts.data.remote.RepuestoDto
import com.example.autoparts.data.remote.TipoItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepuestoCard(
    item: RepuestoDto,
    onClick: () -> Unit,                // toda la tarjeta es tocable: abre el detalle
    modifier: Modifier = Modifier
) {
    ElevatedCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val url = ApiConfig.urlFoto(item.foto)
                if (url != null) {
                    AsyncImage(
                        model = url,
                        contentDescription = item.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Build, contentDescription = null)
                }
            }

            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(item.nombre, style = MaterialTheme.typography.titleMedium)
                if (!item.descripcion.isNullOrBlank()) {
                    Text(
                        item.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    "$" + "%.2f".format(item.precio),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    if (item.tipo == TipoItem.REPUESTO) "Repuesto · Stock: ${item.stock}"
                    else "Servicio",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}