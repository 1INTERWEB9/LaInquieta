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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    ImageButton ButtonAlmuercito, ButtonChoquis, ButtonQuimbayas, ButtonJet;
    TextView textViewProductos, textViewSaldo, FondoMenu;

    public static String PRODUCTO_ENVIO = "producto_envio";
    public static String CUENTA_ENVIO = "cuenta_envio";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
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
    private static String cuenta = null;
    //-------------------------------------------

    private boolean nick=false;
    private boolean money=false;
    private boolean tanto=false;
    private boolean es = false;
    protected String productoNombre = "";
    protected int precio = 0;
    protected int cantidad=0;
    protected String descripcion = "";
    private String aux = "";
    private String aux3 = "";
    private char aux2=' ';

    private String usuario = "";
    private String clave = "";
    private String saldo = "";
    private String saltos = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        tanto=false;
                        es=false;

                    }

                    if(aux2 == '!')
                    {
                        productoNombre="";
                        nick=true;
                    }

                    if(aux2 == '$')
                    {
                        precio=0;
                        money=true;
                    }

                    if(aux2 == '#')
                    {
                        cantidad=0;
                        tanto=true;
                    }

                    if(aux2 == '@')
                    {
                        descripcion="";
                        es=true;
                    }

                    //System.out.print(aux2);

                    if(nick == true)
                    {
                        if(aux2 != '\n')
                            {
                                if(aux2 != '!')
                                {
                                    productoNombre = productoNombre.concat(Character.toString(aux2));
                                    productoNombre = productoNombre.replaceAll("\\s+", "");
                                    //System.out.println(producto.length());
                                    //System.out.println(producto);
                                }

                            }

                    }

                    if(money == true)
                    {
                        if(aux2 != '\n')
                            {
                                if(aux2 != '$')
                                {
                                    aux = aux.concat(Character.toString(aux2));
                                    aux = aux.replaceAll("\\s+", "");
                                    precio = Integer.parseInt(aux);
                                    //System.out.println(aux.length());
                                    //System.out.println(precio);
                                }

                            }

                    }

                    if(tanto == true)
                    {
                        if(aux2 != '\n')
                        {
                            if(aux2 != '#' )
                            {
                                if(aux2 != '$')
                                {
                                    aux3 = aux3.concat(Character.toString(aux2));
                                    aux3 = aux3.replaceAll("\\s+", "");
                                    cantidad = Integer.parseInt(aux3);
                                    //System.out.println(aux.length());
                                    //System.out.println(cantidad);

                                }


                            }

                        }

                    }

                    if(es == true)
                    {
                        if(aux2 != '\n')
                        {
                            if(aux2 != '@')
                            {
                                descripcion = descripcion.concat(Character.toString(aux2));
                                //descripcion = descripcion.replaceAll("\\s+", "");
                                //System.out.println(producto.length());
                                //System.out.println(producto);
                            }

                        }

                    }


                    if(aux2=='%')
                    {
                        Intent intend = new Intent(MainActivity.this, ProductoDetallado.class);
                        intend.putExtra(CUENTA_ENVIO,(usuario+";"+clave+";"+saldo+";"+saltos));
                        intend.putExtra(PRODUCTO_ENVIO,(productoNombre+";"+precio+";"+cantidad+";"+descripcion));
                        intend.putExtra(EXTRA_DEVICE_ADDRESS, address);
                        startActivity(intend);
                    }


                    //dato = dato.replaceAll("\\s+", "");



                }

            }


        };



        btAdapter = BluetoothAdapter.getDefaultAdapter();
        VerificarEstadoBT();

        ButtonAlmuercito = findViewById(R.id.ButtonAlmuercito);
        ButtonChoquis = findViewById(R.id.ButtonChoquis);
        ButtonQuimbayas = findViewById(R.id.ButtonQuimbayas);
        ButtonJet = findViewById(R.id.ButtonJet);

        textViewProductos = findViewById(R.id.textViewProductos);
        textViewSaldo = findViewById(R.id.textViewSaldo);
        FondoMenu = findViewById(R.id.FondoMenu);


        ButtonAlmuercito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyConexionBT.write("Almuercito.");

                //

            }
        });

        ButtonChoquis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                MyConexionBT.write("Choquis.");

            }
        });

        ButtonQuimbayas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyConexionBT.write("Quimbayas.");

            }
        });

        ButtonJet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyConexionBT.write("Jet.");

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
        cuenta = intent.getStringExtra(InicioSesion.CUENTA_ENVIO);
        address = intent.getStringExtra(InicioSesion.EXTRA_DEVICE_ADDRESS);
        if(address.length()<2)
        {
            address = intent.getStringExtra(ProductoDetallado.EXTRA_DEVICE_ADDRESS);
        }
        if(cuenta.length()<2)
        {
            cuenta = intent.getStringExtra(ProductoDetallado.CUENTA_ENVIO);
        }

        StringTokenizer separador = new StringTokenizer(cuenta, ";");
        usuario = separador.nextToken();
        clave =  separador.nextToken();
        saldo = "$"+separador.nextToken();
        saltos =  separador.nextToken();

        textViewSaldo.setText(saldo);

        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacci??n del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexi??n con el socket Bluetooth.
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
    //est?? disponible y solicita que se active si est?? desactivado
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

    //Crea la clase que permite crear el evento de conexi??n
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
                Toast.makeText(getBaseContext(), "La Conexi??n fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }


}