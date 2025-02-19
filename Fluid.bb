
;instructions: press mousebutton , move mouse

Const FLOWSPEED# = 0.3
Const PRECISION% = 15

Const SIZE = 10
Const GRW = 800 , GRH = 600
Const GRX = (GRW / SIZE)
Const GRY = (GRH / SIZE)

Graphics3D GRW , GRH , 32 , 2
SetBuffer BackBuffer()

Global Camera = CreateCamera()
PositionEntity Camera , 0 , 0 , - (GRW * 0.5) / (SIZE + 1)

Dim VelX#(GRX  , GRY  , 2)
Dim VelY#(GRX  , GRY  , 2)

Dim Red#(GRX  , GRY  , 2)
Dim Grn#(GRX  , GRY  , 2)
Dim Blu#(GRX  , GRY  , 2)

Dim Vertex%(GRX  , GRY)

Global Mesh = MakeMesh()

Function AddVelocity(x , y , vx# , vy#)
	Local xx,yy
	
	For yy = y - 1 To y + 1
		For xx = x - 1 To x + 1
			If xx >= 0 And xx < GRX - 1 And yy >= 0 And yy < GRY - 1
				VelX(xx , yy , 0) = VelX(xx , yy , 0) + vx
				VelY(xx , yy , 0) = VelY(xx , yy , 0) + vy
			EndIf
		Next
	Next   
End Function

Function AddColor(x , y , r , g , b)
	Local xx,yy
	
	For yy = y - 1 To y + 1
		For xx = x - 1 To x + 1
			If xx >= 0 And xx < GRX - 1 And yy >= 0 And yy < GRY - 1
				Red(xx , yy , 0) = r
				Grn(xx , yy , 0) = g
				Blu(xx , yy , 0) = b
			EndIf
		Next
	Next      
End Function

Function Animation( d# )
	Local h = (GRX-1) * (GRY-1) , hh# = 1.0 / h
	Local speed# = d * h * FLOWSPEED
	Local k , x , y , x0 , y0 , x1 , y1
	Local xx# , yy# , s0# , s1# , t0# , t1#
	
   ;Advect
	For y = 0 To GRY - 1
		For x = 0 To GRX - 1
			xx# = x - speed * VelX(x , y , 1)   
			yy# = y - speed * VelY(x , y , 1)
			
			If xx < 0 xx = 0 Else If xx > GRX-1 xx = GRX-1
					If yy < 0 yy = 0 Else If yy > GRY-1 yy = GRY-1
							
							x0 = Floor(xx) : x1 = x0 + 1
							y0 = Floor(yy) : y1 = y0 + 1
							
							s1# = xx - x0 : s0# = 1.0 - s1
							t1# = yy - y0 : t0# = 1.0 - t1
							
							VelX(x , y , 1) = s0 * (t0 * VelX(x0 , y0 , 0) + t1 * VelX(x0 , y1 , 0)) + s1 * (t0 * VelX(x1 , y0 , 0) + t1 * VelX(x1 , y1 , 0))
							VelY(x , y , 1) = s0 * (t0 * VelY(x0 , y0 , 0) + t1 * VelY(x0 , y1 , 0)) + s1 * (t0 * VelY(x1 , y0 , 0) + t1 * VelY(x1 , y1 , 0))
							
							Red(x , y , 1) = s0 * (t0 * Red(x0 , y0 , 0) + t1 * Red(x0 , y1 , 0)) + s1 * (t0 * Red(x1 , y0 , 0) + t1 * Red(x1 , y1 , 0))
							Grn(x , y , 1) = s0 * (t0 * Grn(x0 , y0 , 0) + t1 * Grn(x0 , y1 , 0)) + s1 * (t0 * Grn(x1 , y0 , 0) + t1 * Grn(x1 , y1 , 0))
							Blu(x , y , 1) = s0 * (t0 * Blu(x0 , y0 , 0) + t1 * Blu(x0 , y1 , 0)) + s1 * (t0 * Blu(x1 , y0 , 0) + t1 * Blu(x1 , y1 , 0))            
						Next
					Next
					
   ;Project
					For y = 1 To GRY - 2
						For x = 1 To GRX - 2
							VelY(x , y , 0) =  - 0.5 * h * (VelX(x + 1 , y , 1) - VelX(x - 1 , y , 1) + VelY(x , y + 1 , 1) - VelY(x , y - 1 , 1))
							VelX(x , y , 0) = 0
						Next
					Next   
					For k = 1 To PRECISION
						For y = 1 To GRY - 2
							For x = 1 To GRX - 2
								VelX(x , y , 0) = (VelY(x , y , 0) + VelX(x - 1 , y , 0) + VelX(x + 1 , y , 0) + VelX(x , y - 1 , 0) + VelX(x , y + 1 , 0)) * 0.25
							Next
						Next
					Next
					For y = 1 To GRY - 2
						For x = 1 To GRX - 2
							VelX(x , y , 1) = VelX(x , y , 1) - 0.5 * (VelX(x + 1 , y , 0) - VelX(x - 1 , y , 0)) * hh
							VelY(x , y , 1) = VelY(x , y , 1) - 0.5 * (VelX(x , y + 1 , 0) - VelX(x , y - 1 , 0)) * hh
							
						Next
					Next
					
   ;Diffuse
					For y = 1 To GRY - 2
						For x = 1 To GRX - 2
							VelX(x , y , 0) = VelX(x , y , 1);* 0.98 + (VelX(x - 1 , y , 1) + VelX(x + 1 , y , 1) + VelX(x , y - 1 , 1) + VelX(x , y + 1 , 1)) * (0.25 * 0.02)
							VelY(x , y , 0) = VelY(x , y , 1);* 0.98 + (VelY(x - 1 , y , 1) + VelY(x + 1 , y , 1) + VelY(x , y - 1 , 1) + VelY(x , y + 1 , 1)) * (0.25 * 0.02)
							
							Red(x , y , 0) = Red(x , y , 1)
							Grn(x , y , 0) = Grn(x , y , 1)
							Blu(x , y , 0) = Blu(x , y , 1)                  
						Next
					Next
					
					
   ;Test bounds
					For x = 1 To GRX-2
						VelY(x , 1       , 1) =  Abs(VelY(x , 2       , 1))
						VelY(x , GRY - 2 , 1) = -Abs(VelY(x , GRY - 3 , 1))
					Next
					For y = 1 To GRY-2
						VelX(1       , y , 1) =  Abs(VelX(2       , y , 1))
						VelX(GRX - 2 , y , 1) = -Abs(VelX(GRX - 3 , y , 1))
					Next
End Function

Function MakeMesh()
	Local x , y
	Local mesh = CreateMesh()
	Local surf = CreateSurface(Mesh)
	
	EntityFX mesh , 1 + 2
	PositionEntity mesh , 0.5 - GRX * 0.5 , 0.5 - GRY * 0.5, 0
	
	For y = 0 To GRY - 1
		For x = 0 To GRX - 1
			Vertex(x , y) = AddVertex(surf , x  ,  GRY - y - 1  , 0 , x / Float(GRX) , 1.0 - y / Float(GRY))
			VertexColor surf , Vertex(x , y) , 255 , 0 , 0
		Next
	Next
	For y = 0 To GRY - 2
		For x = 0 To GRX - 2
			AddTriangle surf , Vertex(x , y) , Vertex(x + 1 , y) , Vertex(x + 1 , y + 1)
			AddTriangle surf , Vertex(x , y) , Vertex(x + 1 , y + 1) , Vertex(x , y + 1)
		Next
	Next
	
	Return mesh
End Function

Function DrawMesh()
	Local surface = GetSurface(Mesh , 1)
	Local x , y
	
	For y = 0 To GRY - 1
		For x = 0 To GRX - 1
			VertexColor surface , Vertex(x , y) , Red(x , y , 0) , Grn(x , y , 0) , Blu(x , y , 0)
		Next
	Next
End Function

Function Clear()
	Local x , y
	
	For y = 0 To GRY - 1
		For x = 0 To GRX - 1
			VelX(x , y , 0) = 0
			VelY(x , y , 0) = 0
			Red(x , y , 0) = 0
			Grn(x , y , 0) = 0
			Blu(x , y , 0) = 0            
		Next
	Next
End Function

MoveMouse GRW * 0.5 , GRH * 0.5
While (GetKey() = 0)
	timeStep# = (MilliSecs() - oldTime) / 1000.0
	oldTime = MilliSecs()
	
	x = MouseX() / SIZE
	y = MouseY() / SIZE
	mxs# = MouseXSpeed() * timeStep
	mys# = MouseYSpeed() * timeStep
	
	If mxs <> 0 Or mys <> 0 AddVelocity(x , y , mxs , mys)
		
		If MouseHit(1) Color Rnd(255) , Rnd(255) , Rnd(255)
			If MouseDown(1)   AddColor (x , y , ColorRed() * 2 , ColorGreen() * 2 , ColorBlue() * 2)
				If MouseDown(2) Clear()
					
					Animation(timeStep)
					
					DrawMesh()
					
					RenderWorld
					fps = fps + 1 :   If (oldTime - fpst) > 1000 fpst = oldTime : f = fps : fps = 0
						Text 0 , 0 , "fps: " + f
						Flip 0
						
					Wend 
;~IDEal Editor Parameters:
;~C#Blitz3D