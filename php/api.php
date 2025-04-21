<?php
$servername = "localhost";
$username = "Xlbilbao040";
$password = "AiLq63dHFf";
$dbname = "Xlbilbao040_RedJack";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Conexión fallida: " . $conn->connect_error);
}

//obtener los datos JSON del cuerpo de la petición
$inputJSON = file_get_contents("php://input");
$input = json_decode($inputJSON, true); //decodificar a un array asociativo

//ruta para insertar un usuario
if ($_SERVER["REQUEST_METHOD"] === "POST" && isset($input["accion"]) && $input["accion"] === "insertar") {
    $usuario = isset($input["usuario"]) ? $input["usuario"] : "";
    $password = isset($input["pw"]) ? $input["pw"] : "";
    $monedas = isset($input["monedas"]) ? intval($input["monedas"]) : 0;

    if (!empty($usuario) && !empty($password)) {
        //$password = decryptPassword($password);
        $hashedPassword = password_hash($password, PASSWORD_DEFAULT);
        
        //comprobar que no existe
        $stmt = $conn->prepare("SELECT Id FROM Usuarios WHERE nombre = ?");
        $stmt->bind_param("s", $usuario);
        $stmt->execute();
        $stmt->bind_result($posible_id);
        $stmt->fetch();
        $stmt->close();

        if(!$posible_id){
            $stmt = $conn->prepare("INSERT INTO Usuarios (Nombre, Pw, Coins) VALUES (?, ?, ?)");
            $stmt->bind_param("ssi", $usuario, $hashedPassword, $monedas);

            if ($stmt->execute()) {
                echo json_encode(["status" => "success", "code" => "0", "message" => "Usuario agregado correctamente"]);
            } else {
                echo json_encode(["status" => "error", "code" => "1", "message" => "Error al agregar usuario"]);
            }
            $stmt->close();
        }else{
            echo json_encode(["status" => "error", "code" => "2", "message" => "Nombre de usuario ya en uso."]);
        }

        
    } else {
        echo json_encode(["status" => "error", "code" => "4", "message" => "Datos incompletos"]);
    }
}
//ruta para eliminar un usuario por ID
else if ($_SERVER["REQUEST_METHOD"] === "POST" && isset($input["accion"]) && $input["accion"] === "eliminar") {
    $id = isset($input["id"]) ? intval($input["id"]) : 0;
    
    if ($id > 0) {
        $stmt = $conn->prepare("DELETE FROM Usuarios WHERE Id = ?");
        $stmt->bind_param("i", $id);

        if ($stmt->execute()) {
            echo json_encode(["status" => "success", "code" => "0", "message" => "Usuario eliminado correctamente"]);
        } else {
            echo json_encode(["status" => "error", "code" => "1", "message" => "Error al eliminar usuario"]);
        }
        $stmt->close();
    } else {
        echo json_encode(["status" => "error", "code" => "2", "message" => "ID inválido"]);
    }
}
//ruta para verificar credenciales (login)
else if ($_SERVER["REQUEST_METHOD"] === "POST" && isset($input["accion"]) && $input["accion"] === "login") {
    $usuario = isset($input["usuario"]) ? $input["usuario"] : "";
    $password = isset($input["pw"]) ? $input["pw"] : "";
    
    if (!empty($usuario) && !empty($password)) {
        //$password = decryptPassword($password);
        
        $stmt = $conn->prepare("SELECT Pw FROM Usuarios WHERE nombre = ?");
        $stmt->bind_param("s", $usuario);
        $stmt->execute();
        $stmt->bind_result($hash_guardado);
        $stmt->fetch();
        $stmt->close();

        if ($hash_guardado && password_verify($password, $hash_guardado)) {

            $stmt = $conn->prepare("SELECT Id,Nombre,Coins FROM Usuarios WHERE nombre = ?");
            $stmt->bind_param("s", $usuario);
            $stmt->execute();
            $stmt->bind_result($id, $nom, $coins);
            $stmt->fetch();
            $stmt->close();

            echo json_encode(["status" => "success", "code" => "0", "message" => "Login exitoso", "id" => $id, "nombre" => $nom, "monedas" => $coins]);
        } else {
            echo json_encode(["status" => "error", "code" => "1", "message" => "Credenciales incorrectas"]);
        }
    } else {
        echo json_encode(["status" => "error", "code" => "2", "message" => "Datos incompletos"]);
    }
}else if($_SERVER["REQUEST_METHOD"] === "POST" && isset($input["accion"]) && $input["accion"] === "info"){
    $usuario = isset($input["usuario"]) ? $input["usuario"] : "";
    if (!empty($usuario)) {
        $stmt = $conn->prepare("SELECT Nombre, Coins, Victorias, Derrotas, Empates FROM Usuarios WHERE nombre = ?");
        $stmt->bind_param("s", $usuario);
        $stmt->execute();
        $stmt->bind_result($nom, $coins, $victs, $derrs, $empts);
        $stmt->fetch();
        $stmt->close();

        echo json_encode(["status" => "success", "code" => "0", "message" => "Datos obtenidos.", "nombre" => $nom, "monedas" => $coins, "victs" => $victs, "derrs" => $derrs, "empts" => $empts]);
    }else{
        echo json_encode(["status" => "error", "code" => "1", "message" => "Datos insuficientes."]);
    }

}else{
    echo json_encode(["status" => "error", "code" => "-1", "message" => "Buscas algo?"]);
}

$conn->close();
?>
