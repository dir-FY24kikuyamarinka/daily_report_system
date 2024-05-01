package actions;

import java.io.IOException;
import actions.views.EmployeeView;
import constants.MessageConst;

import javax.servlet.ServletException;

import constants.ForwardConst;

/**
 * エラー発生時の処理行うActionクラス
 *
 */
public class UnknownAction extends ActionBase {

    /**
     * 共通エラー画面「お探しのページは見つかりませんでした。」を表示する
     */
    @Override
    public void process() throws ServletException, IOException {

        //エラー画面を表示
        forward(ForwardConst.FW_ERR_UNKNOWN);

        /**
         * 新規登録を行う
         * @throws ServletException
         * @throws IOException
         */
        public void create() throws ServletException, IOException {

            //CSRF対策 tokenのチェック
            if (checkToken()) {

                //日報の日付が入力されていなければ、今日の日付を設定
                LocalDate day = null;
                if (getRequestParam(AttributeConst.REP_DATE) == null
                        || getRequestParam(AttributeConst.REP_DATE).equals("")) {
                    day = LocalDate.now();
                } else {
                    day = LocalDate.parse(getRequestParam(AttributeConst.REP_DATE));
                }

                //セッションからログイン中の従業員情報を取得
                EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

                //パラメータの値をもとに日報情報のインスタンスを作成する
                ReportView rv = new ReportView(
                        null,
                        ev, //ログインしている従業員を、日報作成者として登録する
                        day,
                        getRequestParam(AttributeConst.REP_TITLE),
                        getRequestParam(AttributeConst.REP_CONTENT),
                        null,
                        null);

                //日報情報登録
                List<String> errors = service.create(rv);

                if (errors.size() > 0) {
                    //登録中にエラーがあった場合

                    putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                    putRequestScope(AttributeConst.REPORT, rv);//入力された日報情報
                    putRequestScope(AttributeConst.ERR, errors);//エラーのリスト

                    //新規登録画面を再表示
                    forward(ForwardConst.FW_REP_NEW);

                } else {
                    //登録中にエラーがなかった場合

                    //セッションに登録完了のフラッシュメッセージを設定
                    putSessionScope(AttributeConst.FLUSH, MessageConst.I_REGISTERED.getMessage());

                    //一覧画面にリダイレクト
                    redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);
                }
            }
        } 
    }
}
