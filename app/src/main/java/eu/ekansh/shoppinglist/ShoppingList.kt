package eu.ekansh.shoppinglist

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController

@Composable
fun ShopList(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    navController: NavController,
    context: Context,
    address : String
){

    var sItems by remember { mutableStateOf(listOf<ShoppingItem>()) }
    var showDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("1") }


    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if(permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                // i have access to the permsiion

                locationUtils.requestLocationUpdates(viewModel = viewModel)
            } else {
                //ask for permission

                val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if(rationaleRequired){
                    Toast.makeText(context,
                        "User permission is required for this feature", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context,
                        "Go to the Setting and please give permission", Toast.LENGTH_LONG).show()

                }
            }
        })

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "Shopping List")
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { showDialog = true },
            Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Add Item")
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
            items(sItems){
                item ->
                if(item.isEdited){
                    ShoppingItemEditor(item = item, onEditComplete = {
                        editedName, editedQuantity ->
                        sItems = sItems.map{
                            it.copy(isEdited = false)
                        }

                        // to save the changes of the edited item we are finding the id of that item

                        val editedItem = sItems.find { it.id == item.id }
                        editedItem?.let {
                            it.name = editedName
                            it.quantity = editedQuantity
                            it.address = address
                        }
                    })
                } else {
                    ShoppingListItem(
                        item = item,
                        onEditClick = {
                        sItems = sItems.map{
                            it.copy(isEdited = it.id == item.id) // find which item is clicked for editing
                        }
                    },
                        onDeleteClick = {
                        sItems = sItems-item
                    })
                }
            }
        }
    }

    if(showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {

                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { showDialog = false }) {
                        Text(text = "Cancel")
                    }


                    Button(onClick = {
                        if(itemName.isNotBlank()){
                            val newItem = ShoppingItem(
                                id= sItems.size+1,
                                name = itemName,
                                quantity = itemQuantity.toIntOrNull() ?: 1,
                                address = address
                            )
                            sItems = sItems + newItem
                            showDialog = false
                            itemName = ""
                            itemQuantity = ""
                        }
                    }) {
                        Text(text = "Save")
                    }
                }
                            },
            title = { Text(text = "Add shopping Item")},
            text = {
                Column {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = {itemName = it},
                        label = {Text("Enter the item")},
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = {itemQuantity = it},
                        label = {Text("Enter the Quantity")},
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    
                    Button(onClick = {
                        if(locationUtils.hasLocationPermission(context)) {
                            locationUtils.requestLocationUpdates(viewModel)
                            navController.navigate("locationScreen"){
                                this.launchSingleTop
                            }
                        }else {
                            requestPermissionLauncher.launch(arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ))
                        }
                    }) {
                            Text("Address")
                    }
                }
            }
            )
    }
}

@Composable
fun ShoppingListItem(
    item : ShoppingItem,
    onEditClick : () -> Unit,
    onDeleteClick : () -> Unit
){
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(
                border = BorderStroke(2.dp, Color.Black),
                shape = RoundedCornerShape(20)
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column (
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ){
            Row {
                Text(text = item.name, modifier = Modifier.padding(8.dp))

                Text(text = item.quantity.toString(), modifier = Modifier.padding(8.dp))
            }

            Row (
                modifier = Modifier.fillMaxWidth()
            ){
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null )
                Text(text = item.address)
            }
        }

        Row (
            modifier = Modifier.padding(8.dp)
        ){
            IconButton(onClick = onEditClick) {
                Icon(imageVector = Icons.Default.Edit,contentDescription = null)
            }

            IconButton(onClick = onDeleteClick) {
                Icon(imageVector = Icons.Default.Delete,contentDescription = null)
            }
        }
    }

}


@Composable
fun ShoppingItemEditor(
    item: ShoppingItem,
    onEditComplete : (String,Int) -> Unit
) {
    var editedname by remember{ mutableStateOf(item.name) }
    var editedQauntity by remember{ mutableStateOf(item.quantity.toString()) }
    var isEditing by remember { mutableStateOf(item.isEdited) }

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        Column {
            OutlinedTextField(
                value = editedname,
                onValueChange = {editedname = it},
                label = {Text("Enter the item")},
                singleLine = true
            )

            OutlinedTextField(
                value = editedQauntity,
                onValueChange = {editedQauntity = it},
                label = {Text("Enter the item")},
                singleLine = true
            )
        }
        
        Button(onClick = {
            isEditing = false
            onEditComplete(editedname,editedQauntity.toIntOrNull()?: 1)
        }) {
            Text(text = "Save")
        }
    }
}