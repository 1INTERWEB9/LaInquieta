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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class InicioSesion extends AppCompatActivity
{
    Button buttonIngreso, buttonRegistro;
    EditText editTextUsuario, editTextContra;
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
    //-------------------------------------------
    private boolean nick=false;
    private boolean contra=false;
    private boolean money=false;

    private String usuario = "";
    private String clave = "";
    private int saldo = 0;
    private String saltos="";

    private String aux = "";
    private String aux3 = "";
    private char aux2=' ';
    private boolean sal=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicio_sesion);

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
                        saldo=0;
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
                                System.out.println(usuario);
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
                                System.out.println(clave);
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
                                saldo = Integer.parseInt(aux);
                                //System.out.println(aux.length());
                                System.out.println(saldo);
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
                        Intent intend = new Intent(InicioSesion.this, MainActivity.class);
                        intend.putExtra(CUENTA_ENVIO,(usuario+";"+clave+";"+saldo+";"+saltos));
                        intend.putExtra(EXTRA_DEVICE_ADDRESS, address);
                        startActivity(intend);
                    }


                }

            }


        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        VerificarEstadoBT();

        buttonIngreso = findViewById(R.id.buttonIngreso);
        buttonRegistro= findViewById(R.id.buttonRegistro);
        editTextUsuario= findViewById(R.id.editTextUsuario);
        editTextContra= findViewById(R.id.editTextContra);


        buttonIngreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyConexionBT.write("Usuario.");
                String aux = String.valueOf(editTextUsuario.getText()+",")+String.valueOf(editTextContra.getText())+".";
                MyConexionBT.write(aux);

                //

            }
        });

        buttonRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {


            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);    }

    @Override
    public void onResume()
    {
        super.onResume();

        Intent intent = getIntent();
        address = intent.getStringExtra(DispositivosVinculados.EXTRA_DEVICE_ADDRESS);
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
