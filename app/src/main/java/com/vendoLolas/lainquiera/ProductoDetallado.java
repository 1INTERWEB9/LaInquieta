package com.vendoLolas.lainquiera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.UUID;

public class ProductoDetallado extends AppCompatActivity
{
    TextView textViewProductoEnvio, textViewDescripcion,textViewPrecio, textViewSaldo2;
    ImageView imageViewProducto;
    Button buttonComprar, buttonVolver;
    protected String productoNombre = "";
    protected String precio = "";
    protected String cantidad="";
    protected String descripcion="";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String CUENTA_ENVIO = "cuenta_envio";


    //-------------------------------------------
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address = null;
    private static String producto = null;
    private static String cuenta = null;
    //-------------------------------------------
    private String usuario = "";
    private String clave = "";
    private String saldo = "";
    private String saltos = "";
    private String aux = "";
    private String aux3 = "";
    private char aux2=' ';
    private boolean nick=false;
    private boolean contra=false;
    private boolean money=false;
    private boolean sal=false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.producto_detalles);

        bluetoothIn = new Handler() {
            @SuppressLint("HandlerLeak")
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {

                    aux2=(char) msg.obj;


                    if(aux2 == '\n')
                    {
                        aux="";
                        aux3="";
                        nick=false;
                        money=false;
                        contra=false;
                    }

                    if(aux2 == '!')
                    {
                        usuario="";
                        nick=true;
                    }

                    if(aux2 == '@')
                    {
                        clave="";
                        contra=true;
                    }

                    if(aux2 == '$')
                    {
                        saldo="";
                        money=true;
                    }

                    if(aux2 == '*')
                    {
                        saltos="";
                        sal=true;
                    }

                    if(nick == true)
                    {
                        if(aux2 != '\n')
                        {
                            if(aux2 != '!')
                            {
                                usuario = usuario.concat(Character.toString(aux2));
                                usuario = usuario.replaceAll("\\s+", "");
                                //System.out.println(producto.length());
                                //System.out.println(usuario);
                            }

                        }

                    }

                    if(contra == true)
                    {
                        if(aux2 != '\n')
                        {
                            if(aux2 != '@')
                            {
                                clave = clave.concat(Character.toString(aux2));
                                clave = clave.replaceAll("\\s+", "");
                                //System.out.println(producto.length());
                                //System.out.println(clave);
                            }

                        }

                    }

                    if(money == true)
                    {
                        if(aux2 != '\n')
                        {
                            if(aux2 != '$')
                            {
                                saldo = saldo.concat(Character.toString(aux2));
                                saldo = saldo.replaceAll("\\s+", "");
                                //System.out.println(aux.length());
                                //System.out.println(saldo);
                            }

                        }

                    }

                    if(sal == true)
                    {
                        if(aux2 != '\n')
                        {
                            if(aux2 != '*')
                            {
                                saltos = saltos.concat(Character.toString(aux2));
                                saltos = saltos.replaceAll("\\s+", "");
                                //System.out.println(aux.length());
                                //System.out.println(saldo);
                            }

                        }

                    }

                    if(aux2=='%')
                    {
                        textViewSaldo2.setText("$"+saldo);
                    }

                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        VerificarEstadoBT();

        imageViewProducto = findViewById(R.id.imageViewProducto);
        textViewProductoEnvio = findViewById(R.id.textViewProductoEnvio);
        textViewDescripcion = findViewById(R.id.textViewDescripcion);
        textViewPrecio = findViewById(R.id.textViewPrecio);
        textViewSaldo2 = findViewById(R.id.textViewSaldo2);
        buttonComprar = findViewById(R.id.buttonComprar);
        buttonVolver = findViewById(R.id.buttonVolver);

        buttonComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                precio = precio.replaceAll("\\s+", "");
                saldo = saldo.replaceAll("\\s+", "");
                int i = Integer.parseInt(precio);
                String aux="";
                for(int j=0;j<saldo.length();j++)
                {
                    if(saldo.charAt(j)!='$')
                    {
                        aux += saldo.charAt(j);
                        aux = aux.replaceAll("\\s+", "");
                    }
                }
                System.out.println(aux);
                int k= Integer.parseInt(aux);
                if((k-i)>=0)
                {
                    saldo=String.valueOf((k-i));
                    MyConexionBT.write("Compra.");
                    aux = usuario+","+clave+"$"+saldo+"*"+saltos;
                    MyConexionBT.write(aux);
                }

                //

            }
        });

        buttonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intend2 = new Intent(ProductoDetallado.this, MainActivity.class);
                intend2.putExtra(CUENTA_ENVIO,(usuario+";"+clave+";"+saldo+";"+saltos));
                intend2.putExtra(EXTRA_DEVICE_ADDRESS, address);

                startActivity(intend2);

                //

            }
        });

    }



    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Intent intent = getIntent();
        cuenta = intent.getStringExtra(MainActivity.CUENTA_ENVIO);
        producto = intent.getStringExtra(MainActivity.PRODUCTO_ENVIO);
        address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        StringTokenizer separador2 = new StringTokenizer(cuenta, ";");
        usuario = separador2.nextToken();
        clave =  separador2.nextToken();
        saldo = separador2.nextToken();
        saltos =  separador2.nextToken();


        StringTokenizer separador = new StringTokenizer(producto, ";");
        productoNombre = separador.nextToken();
        precio =  separador.nextToken();
        cantidad = separador.nextToken();
        descripcion = separador.nextToken();


        textViewProductoEnvio.setText(productoNombre);
        textViewPrecio.setText("$"+precio);
        textViewDescripcion.setText(descripcion);

        textViewSaldo2.setText(saldo);

        switch (productoNombre)
        {
            case "Almuercito":
                imageViewProducto.setImageResource(R.drawable.botonalmuercito1);
                break;
            case "Choquis":
                imageViewProducto.setImageResource(R.drawable.botonchokis1);
                break;
            case "Quimbayas":
                imageViewProducto.setImageResource(R.drawable.botonquimbayas1);
                break;
            case "Jet":
                imageViewProducto.setImageResource(R.drawable.botonjet1);
                break;
        }

        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            btSocket.close();
        } catch (IOException e2) {}
    }

    //Comprueba que el dispositivo Bluetooth
    //está disponible y solicita que se active si está desactivado
    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //Crea la clase que permite crear el evento de conexión
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] byte_in = new byte[1];
            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    mmInStream.read(byte_in);
                    char ch = (char) byte_in[0];
                    bluetoothIn.obtainMessage(handlerState, ch).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
