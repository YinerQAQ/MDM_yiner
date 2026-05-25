$headers = @{
    "Content-Type" = "application/json"
}
$body = '{"username":"admin","password":"admin"}'

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -Headers $headers -Body $body
    Write-Host "登录成功!"
    Write-Host "完整响应:"
    $response | ConvertTo-Json
} catch {
    Write-Host "登录失败!"
    Write-Host "状态码: $($_.Exception.Response.StatusCode.value__)"
    $errorStream = $_.Exception.Response.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($errorStream)
    Write-Host "错误信息: $($reader.ReadToEnd())"
}