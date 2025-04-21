<?php
$servername = "localhost";
$username = "Xlbilbao040";
$password = "AiLq63dHFf";
$dbname = "Xlbilbao040_RedJack";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Conexión fallida: " . $conn->connect_error);
}

ini_set('memory_limit', '512M'); // o más si hace falta
ini_set('post_max_size', '64M'); // para permitir posts grandes
ini_set('upload_max_filesize', '64M'); // si subís archivos (por si acaso)
ini_set('max_execution_time', '60'); // segundos, por si tarda
ini_set('max_input_time', '60'); // tiempo de espera para recibir datos

//obtener los datos JSON del cuerpo de la petición
$inputJSON = file_get_contents("php://input");
$input = json_decode($inputJSON, true); //decodificar a un array asociativo

///ruta para obtener pfp
if ($_SERVER["REQUEST_METHOD"] === "POST" && isset($input["accion"]) && $input["accion"] === "getpfp") {
    $nombre = isset($input["nombre"]) ? $input["nombre"] : "";

    if (!empty($nombre)) {
        // Obtener imagen desde la BD
        $stmt = $conn->prepare("SELECT Foto FROM Usuarios WHERE Nombre = ?");
        $stmt->bind_param("s", $nombre);

        if($stmt->execute()){
            $stmt->bind_result($foto_binaria);
            $stmt->fetch();
            $stmt->close();

            $urlImagenServidor = "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . "/pfps/" . $nombre . ".jpg";

            if ($foto_binaria !== null) {
                $image = imagecreatefromstring($foto_binaria);

                if ($image !== false) {
                    ob_start();
                    imagejpeg($image, null, 50); //comprimir
                    $compressedImage = ob_get_clean();
                    imagedestroy($image);

                    $encoded = base64_encode($compressedImage);
                    header('Content-Type: application/json; charset=utf-8');
                    echo json_encode([
                        "status" => "success",
                        "code" => "0",
                        "message" => "Foto obtenida.",
                        "url" => $urlImagenServidor,
                        "imagen" => $encoded
                    ]);
                } else {
                    //si no se pudo procesar la imagen desde BD
                    echo json_encode([
                        "status" => "error",
                        "code" => "4",
                        "message" => "Error al procesar imagen desde BD.",
                        "url" => $urlImagenServidor,
                        "imagen" => null
                    ]);
                }
            } else {
                //si no hay imagen en la BD, se puede intentar usar solo la URL del archivo si existe
                echo json_encode([
                    "status" => "success",
                    "code" => "0",
                    "message" => "No hay imagen en base de datos, se puede usar URL si existe archivo.",
                    "url" => $urlImagenServidor,
                    "imagen" => null
                ]);
            }

        } else {
            echo json_encode(["status" => "error", "code" => "1", "message" => "Error al obtener imagen."]);
        }

    } else {
        echo json_encode(["status" => "error", "code" => "2", "message" => "Datos incompletos"]);
    }

//ruta establecer pfp
/*}else if ($_SERVER["REQUEST_METHOD"] === "POST" && isset($input["accion"]) && $input["accion"] === "setpfp") {
    $pic = isset($input["pic"]) ? $input["pic"] : "";
    $id = isset($input["id"]) ? intval($input["id"]) : 0;

    if (!empty($pic) && !empty($id)) {
        //comprobar que no existe
        $picbinaria = base64_decode($pic);
        $stmt = $conn->prepare("UPDATE Usuarios SET Foto=? WHERE Id = ?");
        $stmt->bind_param("si", $picbinaria, $id);
        if($stmt->execute()){
            echo json_encode(["status" => "error", "code" => "0", "message" => "Imagen establecida."]);
        }else{
            echo json_encode(["status" => "error", "code" => "1", "message" => "Error al establecer imagen."]);
        }
        $stmt->close();
        
    } else {
        echo json_encode(["status" => "error", "code" => "2", "message" => "Datos incompletos"]);
    }*/

}else if ($_SERVER["REQUEST_METHOD"] === "POST" && isset($input["accion"]) && $input["accion"] === "setpfp") {
    $pic = isset($input["pic"]) ? $input["pic"] : "";
    $id = isset($input["id"]) ? intval($input["id"]) : 0;

    if (!empty($pic) && !empty($id)) {
        $picbinaria = base64_decode($pic);
        
        // Crear imagen original desde binario
        $imagenOriginal = imagecreatefromstring($picbinaria);
        if (!$imagenOriginal) {
            echo json_encode(["status" => "error", "code" => "4", "message" => "No se pudo leer la imagen."]);
            exit;
        }

        // Obtener tamaño original
        $anchoOriginal = imagesx($imagenOriginal);
        $altoOriginal = imagesy($imagenOriginal);

        // Nueva resolución (por ejemplo 256x256 máx)
        $nuevoAncho = 56;
        $nuevoAlto = intval($altoOriginal * ($nuevoAncho / $anchoOriginal));

        // Crear imagen redimensionada
        $imagenReducida = imagecreatetruecolor($nuevoAncho, $nuevoAlto);
        imagecopyresampled($imagenReducida, $imagenOriginal, 0, 0, 0, 0, $nuevoAncho, $nuevoAlto, $anchoOriginal, $altoOriginal);

        // Guardar imagen redimensionada como JPEG comprimido en memoria
        ob_start();
        imagejpeg($imagenReducida, null, 70); // 70% calidad, ajustable
        $imagenFinal = ob_get_clean();

        // Liberar memoria
        imagedestroy($imagenOriginal);
        imagedestroy($imagenReducida);

        // Obtener el nombre de usuario
        $stmtNombre = $conn->prepare("SELECT Nombre FROM Usuarios WHERE Id = ?");
        $stmtNombre->bind_param("i", $id);
        $stmtNombre->execute();
        $stmtNombre->bind_result($nombreUsuario);
        $stmtNombre->fetch();
        $stmtNombre->close();

        if (!$nombreUsuario) {
            echo json_encode(["status" => "error", "code" => "3", "message" => "Usuario no encontrado."]);
            exit;
        }

        // Guardar imagen reducida en la base de datos
        $stmt = $conn->prepare("UPDATE Usuarios SET Foto=? WHERE Id = ?");
        $stmt->bind_param("si", $imagenFinal, $id);
        $bdOk = $stmt->execute();
        $stmt->close();

        // Guardar imagen en el servidor
        $carpeta = __DIR__ . "/pfps/";
        if (!is_dir($carpeta)) {
            mkdir($carpeta, 0777, true);
        }
        $filename = $carpeta . $nombreUsuario . ".jpg";
        $fileOk = file_put_contents($filename, $imagenFinal);

        // URL pública
        $url = "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . "/pfps/" . $nombreUsuario . ".jpg";

        if ($bdOk && $fileOk !== false) {
            echo json_encode([
                "status" => "success",
                "code" => "0",
                "message" => "Imagen redimensionada y establecida.",
                "url" => $url
            ]);
        } else {
            echo json_encode(["status" => "error", "code" => "1", "message" => "Error al guardar imagen."]);
        }

    } else {
        echo json_encode(["status" => "error", "code" => "2", "message" => "Datos incompletos"]);
    }
}else{
    echo json_encode(["status" => "error", "code" => "-1", "message" => "Buscas algo?"]);
}

$conn->close();
?>
