## Mô tả bài toán chung
- Hệ thống thực hiện call function A với tham số Long id xác định.
- Nếu kết quả call function A(Long id) thành công thì kết thúc luồng xử lý này.
- Nếu kết quả call function A(Long id) không thành công thì thực hiện bước sau:
- Kiểm tra việc call function A(Long id) đã quá số lần cho phép retry chưa?
- Nếu đã quá số lần retry cho phép thì ghi log hệ thống, kết thúc luồng xử lý với tham số id.
- Nếu chưa quá số lần retry cho phép thì thực hiện đẩy call function A(Long id) vào Delay Queue với thời gian delay đã được cấu hình từ trước.
- Khi item trong Delay Queue đến thời điểm xử lý (thời gian delay hết hạn - time delay is expired), thực hiện lại call function A với tham số id xác định.

## Sơ đồ luồng xử lý
![DLQ_flow](https://github.com/ThangLuffy/DLQExample/assets/27936180/859e64a6-f139-4a6a-b829-0eef08c5386a)
